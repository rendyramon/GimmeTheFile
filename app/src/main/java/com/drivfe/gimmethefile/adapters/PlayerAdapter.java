package com.drivfe.gimmethefile.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drivfe.gimmethefile.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.ViewHolder> {
    private List<ResolveInfo> mAppsList;
    private Context mContext;
    private PlayerItemClickListener mListener;

    public PlayerAdapter(Context context, PlayerItemClickListener listener) {
        mContext = context;
        mListener = listener;
        PackageManager pm = mContext.getPackageManager();
        Intent open = new Intent(Intent.ACTION_VIEW);
        open.setType("video/*");

        mAppsList = pm.queryIntentActivities(open, 0);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.openplayer_list_item, parent, false);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = ((ViewHolder)v.getTag()).position;
                mListener.onItemClicked(mAppsList.get(position).activityInfo);
            }
        });

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mPlayerName.setText(mAppsList.get(position).loadLabel(mContext.getPackageManager()));
        holder.mLogo.setImageDrawable(mAppsList.get(position).loadIcon(mContext.getPackageManager()));
        holder.position = position;
    }

    @Override
    public int getItemCount() {
        return mAppsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_openplayer_name) TextView mPlayerName;
        @Bind(R.id.iv_openplayer_logo) ImageView mLogo;
        int position;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setTag(this);
        }
    }

    public interface PlayerItemClickListener {
        void onItemClicked(ActivityInfo ai);
    }
}
