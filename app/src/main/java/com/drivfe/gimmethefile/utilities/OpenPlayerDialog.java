package com.drivfe.gimmethefile.utilities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.drivfe.gimmethefile.R;
import com.drivfe.gimmethefile.adapters.PlayerAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class OpenPlayerDialog extends AppCompatDialog {
    @Bind(R.id.rv_openplayer) RecyclerView mPlayersList;
    private PlayerAdapter mAdapter;
    private Context mContext;

    public OpenPlayerDialog(Context context, String url, PlayerAdapter.PlayerItemClickListener listener) {
        super(context);
        mContext = context;
        View v = ((Activity) mContext).getLayoutInflater().inflate(R.layout.openplayerdialog, null);
        ButterKnife.bind(this, v);
        setContentView(v);
        setTitle("Open with...");

        mPlayersList.setHasFixedSize(true);
        mPlayersList.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new PlayerAdapter(mContext, url, listener);
        mPlayersList.setAdapter(mAdapter);

        Point p = new Point();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getSize(p);
        p.x = p.x / 100 * 85;
        getWindow().setLayout(p.x, getWindow().getAttributes().height);
    }
}
