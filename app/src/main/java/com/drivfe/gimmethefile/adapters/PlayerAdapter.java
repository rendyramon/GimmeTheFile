package com.drivfe.gimmethefile.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.drivfe.gimmethefile.R;
import com.drivfe.gimmethefile.databinding.OpenPlayerListItemBinding;

import java.util.List;

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
        View v = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.open_player_list_item, parent, false).getRoot();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.getBinding().tvOpenplayerName.setText(mAppsList.get(position).loadLabel(mContext.getPackageManager()));
        holder.getBinding().ivOpenplayerLogo.setImageDrawable(mAppsList.get(position).loadIcon(mContext.getPackageManager()));
    }

    @Override
    public int getItemCount() {
        return mAppsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private OpenPlayerListItemBinding binding;

        ViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.findBinding(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onItemClicked(mAppsList.get(getAdapterPosition()).activityInfo);
                }
            });
        }

        public OpenPlayerListItemBinding getBinding() {
            return binding;
        }
    }

    public interface PlayerItemClickListener {
        void onItemClicked(ActivityInfo ai);
    }
}
