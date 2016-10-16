package com.drivfe.gimmethefile.utilities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.drivfe.gimmethefile.R;
import com.drivfe.gimmethefile.activities.FormatsActivity;
import com.drivfe.gimmethefile.adapters.PlayerAdapter;
import com.drivfe.gimmethefile.models.MediaFileBucket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DialogUtils {
    public static void showBucketInfoDialog(final Activity activity, MediaFileBucket bucket) {
        StringBuilder sb = new StringBuilder();
        List<String> fieldsToIgnore = new ArrayList<>();
        fieldsToIgnore.add("url");
        fieldsToIgnore.add("id");
        fieldsToIgnore.add("thumbnail");
        fieldsToIgnore.add("serialVersionUID");

        for (Map.Entry<String, Object> entry : HelperUtils.getClassFields(bucket).entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (key.equals("formats")) {
                key = "formats available";
                value = bucket.formats.size();
            }

            if (key.equals("filesize") && value != null)
                value = Formatter.formatFileSize(activity, (Long) value);

            if (key.equals("duration") && value != null)
                value = value + " seconds";

            if (fieldsToIgnore.indexOf(key) == -1 && value != null)
                sb.append("<p><b><font color='black'>").append(key).append(":</font></b> ").append(value.getClass().cast(value)).append("</p>");
        }
        Spanned headers = Html.fromHtml(sb.toString());

        new MaterialDialog.Builder(activity)
                .title("More information")
                .content(headers)
                .positiveText("Ok")
                .show();
    }

    public static void showErrorDialog(final Activity activity, String error, String more, final Boolean finish) {
        StringBuilder errormsgB = new StringBuilder().append(error);
        if (more != null)
            errormsgB.append("\n\n").append(more);

        new MaterialDialog.Builder(activity)
                .title("Woops")
                .content(errormsgB.toString())
                .positiveText("Ok")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (finish)
                            activity.finish();
                    }
                })
                .cancelable(false)
                .show();
    }

    public static void showErrorDialog(final Activity activity, String error, String more) {
        showErrorDialog(activity, error, more, true);
    }

    public static void showLinkInputDialog(final Activity activity) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_link_input, null);
        dialogBuilder.setView(dialogView);

        final TextInputLayout til_link = (TextInputLayout) dialogView.findViewById(R.id.til_link);
        til_link.setHint("Link");

        final AlertDialog b = dialogBuilder
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Paste", null)
                .setPositiveButton("Continue", null)
                .create();

        b.show();

        Button btnContinue = b.getButton(AlertDialog.BUTTON_POSITIVE);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String link = til_link.getEditText().getText().toString();
                if (!HelperUtils.isValidUrl(link)) {
                    til_link.setError("Invalid URL");
                } else {
                    activity.startActivity(FormatsActivity.newIntent(activity, link));
                    b.dismiss();
                }
            }
        });

        final ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        Button btnNeutral = b.getButton(AlertDialog.BUTTON_NEUTRAL);
        if (!cm.hasPrimaryClip() || !cm.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            btnNeutral.setEnabled(false);
        }

        btnNeutral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData.Item item = cm.getPrimaryClip().getItemAt(0);
                String link = item.getText().toString();
                til_link.getEditText().setText(link);
            }
        });
    }

    public static void showOpenPlayerDialog(Context context, PlayerAdapter.PlayerItemClickListener listener) {
        new OpenPlayerDialog(context, listener).show();
    }
}