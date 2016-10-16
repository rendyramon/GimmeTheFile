package com.drivfe.gimmethefile.utilities;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;

import com.drivfe.gimmethefile.R;
import com.drivfe.gimmethefile.adapters.PlayerAdapter;
import com.drivfe.gimmethefile.databinding.OpenPlayerDialogBinding;

class OpenPlayerDialog extends AppCompatDialog {
    OpenPlayerDialog(Context context, PlayerAdapter.PlayerItemClickListener listener) {
        super(context);
        OpenPlayerDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.open_player_dialog, null, false);
        setContentView(binding.getRoot());
        setTitle("Open with...");

        binding.rvOpenplayer.setHasFixedSize(true);
        binding.rvOpenplayer.setLayoutManager(new LinearLayoutManager(context));
        binding.rvOpenplayer.setAdapter(new PlayerAdapter(context, listener));

        Point p = new Point();
        ((Activity) context).getWindowManager().getDefaultDisplay().getSize(p);
        p.x = p.x / 100 * 85;
        getWindow().setLayout(p.x, getWindow().getAttributes().height);
    }
}
