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

public class OpenPlayerDialog extends AppCompatDialog {
    public OpenPlayerDialog(Context context, PlayerAdapter.PlayerItemClickListener listener) {
        super(context);
        View v = ((Activity) context).getLayoutInflater().inflate(R.layout.openplayerdialog, null);
        RecyclerView playersList = (RecyclerView) v.findViewById(R.id.rv_openplayer);
        setContentView(v);
        setTitle("Open with...");

        playersList.setHasFixedSize(true);
        playersList.setLayoutManager(new LinearLayoutManager(context));
        playersList.setAdapter(new PlayerAdapter(context, listener));

        Point p = new Point();
        ((Activity) context).getWindowManager().getDefaultDisplay().getSize(p);
        p.x = p.x / 100 * 85;
        getWindow().setLayout(p.x, getWindow().getAttributes().height);
    }
}
