package com.drivfe.gimmethefile.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;

import com.drivfe.gimmethefile.R;
import com.drivfe.gimmethefile.adapters.FormatsAdapter;
import com.drivfe.gimmethefile.adapters.PlayerAdapter;
import com.drivfe.gimmethefile.databinding.ActivityFormatsBinding;
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

    private ActivityFormatsBinding binding;
    private MediaFileBucket mBucket;
    private String mLink;
    private CompositeSubscription mSubscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_formats);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.collapsingFormats.setExpandedTitleMarginStart(48);
        binding.collapsingFormats.setExpandedTitleMarginEnd(64);
        binding.collapsingFormats.setContentScrimColor(ContextCompat.getColor(this, R.color.colorPrimary));
        binding.collapsingFormats.setTitle("Formats");
        binding.collapsingFormats.setExpandedTitleTextAppearance(R.style.CollapsingExpandedTitle);

        binding.fabFormats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBucket != null)
                    DialogUtils.showBucketInfoDialog(FormatsActivity.this, mBucket);
            }
        });

        binding.formatsContent.btnFormatsRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processLink();
            }
        });

        binding.formatsContent.rvFormats.setHasFixedSize(true);
        binding.formatsContent.rvFormats.setLayoutManager(new LinearLayoutManager(this));

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
    public void onFormatShareClicked(int position) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, mBucket.formats.get(position).url);
        i.setType("text/plain");
        startActivity(Intent.createChooser(i, "Share direct link!"));
    }

    @Override
    public void onFormatDownloadClicked(int position) {
        if (!DownloadService.isRunning) {
            startActivity(DownloadActivity.newIntent(this, new DownloadEntry(mBucket, mBucket.formats.get(position))));
            finish();
        } else {
            DialogUtils.showErrorDialog(this, "There is already a download in progress. Please wait until it is finished.", null, false);
        }
    }

    private void populateRecyclerView() {
        if (mBucket.thumbnail != null)
            Picasso.with(this)
                    .load(mBucket.thumbnail)
                    .fit()
                    .centerCrop()
                    .into(binding.ivFormats);

        binding.collapsingFormats.setTitle(mBucket.title);

        binding.formatsContent.rvFormats.setItemViewCacheSize(mBucket.formats.size());
        binding.formatsContent.rvFormats.setAdapter(new FormatsAdapter(this, this, mBucket));

        binding.formatsContent.llPbLoadingFormats.setVisibility(View.GONE);
        binding.formatsContent.rvFormats.setVisibility(View.VISIBLE);
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
            binding.formatsContent.tvFormatLoadingError.setText("Failed to connect, server may be restarting.");
            binding.formatsContent.pbLoading.setVisibility(View.GONE);
            binding.formatsContent.btnFormatsRetry.setVisibility(View.VISIBLE);
        } else {
            DialogUtils.showErrorDialog(this, exc.getDefaultErrorMessage(), exc.getDefaultErrorMessageMore());
        }
    }

    private void processLink() {
        if (!HelperUtils.isValidUrl(mLink)) {
            DialogUtils.showErrorDialog(this, getString(R.string.invalidurl), null);
        }

        if (!HelperUtils.isNetworkAvailable(this)) {
            Timber.d(getString(R.string.nointernetconnection));
            binding.formatsContent.tvFormatLoadingError.setText(R.string.nointernetconnection);
            binding.formatsContent.pbLoading.setVisibility(View.GONE);
            binding.formatsContent.btnFormatsRetry.setVisibility(View.VISIBLE);
        } else {
            binding.formatsContent.tvFormatLoadingError.setText(getString(R.string.loading_formats));
            binding.formatsContent.pbLoading.setVisibility(View.VISIBLE);
            binding.formatsContent.btnFormatsRetry.setVisibility(View.GONE);

            mSubscriptions.add(getBucketObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<MediaFileBucket>() {
                        @Override
                        public void onCompleted() {
                        }

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