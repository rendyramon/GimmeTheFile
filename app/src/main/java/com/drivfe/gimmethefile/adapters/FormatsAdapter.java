package com.drivfe.gimmethefile.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.drivfe.gimmethefile.R;
import com.drivfe.gimmethefile.listeners.FormatCardListener;
import com.drivfe.gimmethefile.models.MediaFileBucket;
import com.drivfe.gimmethefile.models.MediaFileFormat;
import com.drivfe.gimmethefile.utilities.HelperUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FormatsAdapter extends RecyclerView.Adapter<FormatsAdapter.ViewHolder> {
    private final Context mContext;
    private FormatCardListener mListener;
    private ArrayList<MediaFileFormat> mFormats;
    private static FormatClickListener mClickListener;

    public FormatsAdapter(Context ctx, FormatCardListener listener, MediaFileBucket bucket) {
        mListener = listener;
        mContext = ctx;
        mFormats = bucket.formats;
        mClickListener = new FormatClickListener();
    }

    private Map<Object, Object> createInfoHeaders(MediaFileFormat format) {
        final List<String> fieldsToIgnore = new ArrayList<>();
        fieldsToIgnore.add("url");
        fieldsToIgnore.add("protocol");
        fieldsToIgnore.add("headers");

        final Map<Object, Object> formatInfo = new HashMap<>();

        for (Map.Entry<String, Object> entry : HelperUtils.getClassFields(format).entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (key.equals("filesize") && value != null)
                value = Formatter.formatFileSize(mContext, (Long)value);

            if (key.equals("duration") && value != null)
                value = value +" seconds";

            if (fieldsToIgnore.indexOf(key) == -1 && value != null)
                formatInfo.put(key, value.getClass().cast(value));
        }

        return formatInfo;
    }

    @Override
    public FormatsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.formats_list_item, parent, false);
        return new ViewHolder(v);
    }

    private String iterToString(Iterable s) {
        StringBuilder b = new StringBuilder();
        for (Object v : s)
            b.append(v).append("\n");

        return b.deleteCharAt(b.length()-1).toString();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.position = position;
        MediaFileFormat frm = mFormats.get(position);

        Map<Object, Object> info = createInfoHeaders(frm);

        holder.mFormatKeys.setText(iterToString(info.keySet()));
        holder.mFormatValues.setText(iterToString(info.values()));

        if (position == 0) {
            holder.mRecommended.setVisibility(View.VISIBLE);
            holder.mRecommendedIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mFormats.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_format_info_keys) TextView mFormatKeys;
        @Bind(R.id.tv_format_info_values) TextView mFormatValues;
        @Bind(R.id.btn_format_download) Button mDownload;
        @Bind(R.id.btn_format_open) Button mOpen;
        @Bind(R.id.btn_format_share) Button mShare;
        @Bind(R.id.tv_format_recommended) TextView mRecommended;
        @Bind(R.id.iv_format_best) ImageView mRecommendedIcon;
        public int position;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            mDownload.setOnClickListener(mClickListener);
            mDownload.setTag(this);
            mOpen.setOnClickListener(mClickListener);
            mOpen.setTag(this);
            mShare.setOnClickListener(mClickListener);
            mShare.setTag(this);
        }
    }

    public class FormatClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ViewHolder vh = (ViewHolder) v.getTag();
            switch(v.getId()){
                case R.id.btn_format_download:
                    mListener.onFormatDownloadClicked(vh.position);
                    break;

                case R.id.btn_format_open:
                    mListener.onFormatOpenClicked(vh.position);
                    break;

                case R.id.btn_format_share:
                    mListener.onFormatShareClicked(vh.position);
                    break;
            }
        }
    }
}