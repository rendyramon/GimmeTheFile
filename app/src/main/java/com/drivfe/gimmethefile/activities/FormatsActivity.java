package com.drivfe.gimmethefile.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.drivfe.gimmethefile.R;
import com.drivfe.gimmethefile.adapters.FormatsAdapter;
import com.drivfe.gimmethefile.adapters.PlayerAdapter;
import com.drivfe.gimmethefile.download.DownloadEntry;
import com.drivfe.gimmethefile.download.DownloadService;
import com.drivfe.gimmethefile.errors.BaseException;
import com.drivfe.gimmethefile.errors.FailedToConnectException;
import com.drivfe.gimmethefile.listeners.FormatCardListener;
import com.drivfe.gimmethefile.models.MediaFileBucket;
import com.drivfe.gimmethefile.utilities.DialogUtils;
import com.drivfe.gimmethefile.utilities.HelperUtils;
import com.drivfe.gimmethefile.utilities.RequestUtils;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class FormatsActivity extends AppCompatActivity implements FormatCardListener {
    private static final String EXTRA_LINK = Intent.EXTRA_TEXT;
    private static final String EXTRA_BUCKET = "EXTRA_BUCKET";

    @Bind(R.id.rv_formats) RecyclerView mFormatsList;
    @Bind(R.id.pb_loading) ProgressBar mLoadingBar;
    @Bind(R.id.tv_format_loading_error) TextView mLoadingError;
    @Bind(R.id.btn_formats_retry) Button mRetryButton;
    @Bind(R.id.ll_pb_loading_formats) LinearLayout mLinearLayoutProgress;
    @Bind(R.id.collapsing_formats) CollapsingToolbarLayout mCollapsing;
    @Bind(R.id.iv_formats) ImageView mCollapsingImage;
    @Bind(R.id.fab_formats) FloatingActionButton fab;

    RecyclerView.Adapter mAdapter;
    MediaFileBucket mBucket;
    String mLink;
    CompositeSubscription mSubscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formats);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        mCollapsing.setExpandedTitleMarginStart(48);
        mCollapsing.setExpandedTitleMarginEnd(64);
        mCollapsing.setContentScrimColor(ContextCompat.getColor(this, R.color.colorPrimary));
        mCollapsing.setTitle("Formats");
        mCollapsing.setExpandedTitleTextAppearance(R.style.CollapsingExpandedTitle);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBucket != null)
                    DialogUtils.showBucketInfoDialog(FormatsActivity.this, mBucket);
            }
        });

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processLink();
            }
        });

        mFormatsList.setHasFixedSize(true);
        mFormatsList.setLayoutManager(new LinearLayoutManager(this));

        Bundle extras = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();

        mLink = HelperUtils.extractUrl(extras.getString(EXTRA_LINK));
        mBucket = (MediaFileBucket) extras.getSerializable(EXTRA_BUCKET);

        if (mBucket == null)
            processLink();
        else
            populateRecyclerView();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Timber.d("onSaveInstanceState");
        outState.putSerializable(EXTRA_BUCKET, mBucket);
        outState.putString(EXTRA_LINK, mLink);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onFormatOpenClicked(int position) {
        final String url = mBucket.formats.get(position).url;
        DialogUtils.showOpenPlayerDialog(this, new PlayerAdapter.PlayerItemClickListener() {
            @Override
            public void onItemClicked(ActivityInfo ai) {
                Intent open = new Intent(Intent.ACTION_VIEW);
                open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                open.setType("video/*");
                open.setData(Uri.parse(url));
                open.setComponent(new ComponentName(ai.packageName, ai.name));
                startActivity(open);
            }
        });
    }

    @Override
    public void onFormatDownloadClicked(int position) {
        if (!DownloadService.isRunning) {
            startActivity(DownloadActivity.newIntent(this, new DownloadEntry(mBucket, mBucket.formats.get(position))));
            finish();
        }
        else {
            DialogUtils.showErrorDialog(this, "There is already a download in progress. Please wait until it is finished.", null, false);
        }
    }

    private void populateRecyclerView() {
        if (mBucket.thumbnail != null)
            Picasso.with(this)
                    .load(mBucket.thumbnail)
                    .fit()
                    .centerCrop()
                    .into(mCollapsingImage);

        mCollapsing.setTitle(mBucket.title);

        mFormatsList.setItemViewCacheSize(mBucket.formats.size());
        mAdapter = new FormatsAdapter(this, this, mBucket);
        mFormatsList.setAdapter(mAdapter);

        mLinearLayoutProgress.setVisibility(View.GONE);
        mFormatsList.setVisibility(View.VISIBLE);
    }

    public void bucketReceived(MediaFileBucket bkt) {
        mBucket = bkt;

        if (mBucket.formats.size() == 0) {
            DialogUtils.showErrorDialog(this, "Unsupported formats", "No supported formats are available for this link. Some formats (e.g. m3u8) are not yet supported by " + getString(R.string.app_name) + ".");
        }

        populateRecyclerView();
    }

    public void bucketError(BaseException exc) {
        if (exc instanceof FailedToConnectException) {
            mLoadingError.setText("Failed to connect, server may be restarting.");
            mLoadingBar.setVisibility(View.GONE);
            mRetryButton.setVisibility(View.VISIBLE);
        }
        else {
            DialogUtils.showErrorDialog(this, exc.getDefaultErrorMessage(), exc.getDefaultErrorMessageMore());
        }
    }

    private void processLink() {
        if (!HelperUtils.isValidUrl(mLink)) {
            DialogUtils.showErrorDialog(this, getString(R.string.invalidurl), null);
        }

        if (!HelperUtils.isNetworkAvailable(this)) {
            Timber.d(getString(R.string.nointernetconnection));
            mLoadingError.setText(R.string.nointernetconnection);
            mLoadingBar.setVisibility(View.GONE);
            mRetryButton.setVisibility(View.VISIBLE);
        }

        else {
            mLoadingError.setText(getString(R.string.loading_formats));
            mLoadingBar.setVisibility(View.VISIBLE);
            mRetryButton.setVisibility(View.GONE);

            mSubscriptions.add(getBucketObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<MediaFileBucket>() {
                        @Override
                        public void onCompleted() {}

                        @Override
                        public void onError(Throwable e) {
                            bucketError((BaseException) e);
                        }

                        @Override
                        public void onNext(MediaFileBucket s) {
                            bucketReceived(s);
                        }
                    })
            );
        }
    }

    private Observable<MediaFileBucket> getBucketObservable() {
        return Observable.defer(new Func0<Observable<MediaFileBucket>>() {
            @Override
            public Observable<MediaFileBucket> call() {
                MediaFileBucket bkt;
                try {
                    bkt = RequestUtils.requestJson(HelperUtils.createUrl(mLink));
                } catch (BaseException e) {
                    return Observable.error(e);
                }

                return Observable.just(bkt);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Intent newIntent(Context context, String link) {
        Intent intent = new Intent(context, FormatsActivity.class);
        intent.putExtra(EXTRA_LINK, link);
        return intent;
    }
}