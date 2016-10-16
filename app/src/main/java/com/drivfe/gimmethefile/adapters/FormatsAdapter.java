package com.drivfe.gimmethefile.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.drivfe.gimmethefile.R;
import com.drivfe.gimmethefile.databinding.FormatsListItemBinding;
import com.drivfe.gimmethefile.listeners.FormatCardListener;
import com.drivfe.gimmethefile.models.MediaFileBucket;
import com.drivfe.gimmethefile.models.MediaFileFormat;
import com.drivfe.gimmethefile.utilities.HelperUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        fieldsToIgnore.add("serialVersionUID");

        final Map<Object, Object> formatInfo = new HashMap<>();

        for (Map.Entry<String, Object> entry : HelperUtils.getClassFields(format).entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (key.equals("filesize") && value != null)
                value = Formatter.formatFileSize(mContext, (Long) value);

            if (key.equals("duration") && value != null)
                value = value + " seconds";

            if (fieldsToIgnore.indexOf(key) == -1 && value != null)
                formatInfo.put(key, value.getClass().cast(value));
        }

        return formatInfo;
    }

    @Override
    public FormatsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.formats_list_item, parent, false).getRoot();
        return new ViewHolder(v);
    }

    private String iterToString(Iterable s) {
        StringBuilder b = new StringBuilder();
        for (Object v : s)
            b.append(v).append("\n");

        return b.deleteCharAt(b.length() - 1).toString();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MediaFileFormat frm = mFormats.get(position);

        Map<Object, Object> info = createInfoHeaders(frm);

        holder.getBinding().tvFormatInfoKeys.setText(iterToString(info.keySet()));
        holder.getBinding().tvFormatInfoValues.setText(iterToString(info.values()));

        if (position == 0) {
            holder.getBinding().tvFormatRecommended.setVisibility(View.VISIBLE);
            holder.getBinding().ivFormatBest.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mFormats.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private FormatsListItemBinding binding;

        ViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.findBinding(v);

            binding.btnFormatDownload.setOnClickListener(mClickListener);
            binding.btnFormatDownload.setTag(this);
            binding.btnFormatOpen.setOnClickListener(mClickListener);
            binding.btnFormatOpen.setTag(this);
            binding.btnFormatShare.setOnClickListener(mClickListener);
            binding.btnFormatShare.setTag(this);
        }

        FormatsListItemBinding getBinding() {
            return binding;
        }
    }

    private class FormatClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ViewHolder vh = (ViewHolder) v.getTag();
            switch (v.getId()) {
                case R.id.btn_format_download:
                    mListener.onFormatDownloadClicked(vh.getAdapterPosition());
                    break;

                case R.id.btn_format_open:
                    mListener.onFormatOpenClicked(vh.getAdapterPosition());
                    break;

                case R.id.btn_format_share:
                    mListener.onFormatShareClicked(vh.getAdapterPosition());
                    break;
            }
        }
    }
}