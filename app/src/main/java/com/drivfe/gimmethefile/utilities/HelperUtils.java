package com.drivfe.gimmethefile.utilities;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Patterns;
import android.widget.Toast;

import com.drivfe.gimmethefile.Config;
import com.drivfe.gimmethefile.R;
import com.drivfe.gimmethefile.models.MediaFileBucket;
import com.drivfe.gimmethefile.models.MediaFileFormat;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;

import okhttp3.HttpUrl;
import rx.Observable;
import timber.log.Timber;

public class HelperUtils {
    public static Observable<Boolean> askForWritePermission(Context context) {
        return RxPermissions.getInstance(context)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static MediaFileBucket refactorBucket(MediaFileBucket bucket) {
        Collections.reverse(bucket.formats); // best is last
        for (Iterator<MediaFileFormat> iter = bucket.formats.iterator(); iter.hasNext(); ) {
            MediaFileFormat format = iter.next();
            if (format.protocol.equals("m3u8") || format.protocol.equals("m3u8_native")) {
                iter.remove();
            }
        }

        return bucket;
    }

    public static HashMap<String, Object> getClassFields(Object cls) {
        HashMap<String, Object> fieldsMap = new HashMap<String, Object>();
        try {
            for (Field field : cls.getClass().getFields()) {
                fieldsMap.put(field.getName(), field.get(cls));
            }
        } catch (IllegalAccessException e) {
            Timber.e(e, "getClassFields");
        }

        return fieldsMap;
    }

    public static String extractUrl(String text) {
        Matcher matcher = Patterns.WEB_URL.matcher(text);
        try {
            matcher.find();
            return matcher.group();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public static Boolean isValidUrl(String link) {
        try {
            new URL(extractUrl(link));
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static URL createUrl(String link) {
        HttpUrl.Builder builder = HttpUrl.parse(Config.BASE_URL).newBuilder();
        builder.addQueryParameter("url", link);
        return builder.build().url();
    }

    @SuppressWarnings("deprecation")
    public static Boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = connectivityManager.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = connectivityManager.getNetworkInfo(mNetwork);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }
        } else {
            if (connectivityManager != null) {
                NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void makeToast(Context ctx, String text, Boolean largo) {
        Toast.makeText(ctx.getApplicationContext(), text, largo ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    public static void setAppDownloadDirectory(Context context, String newFolder) {
        SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.settings_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        // We don't need the emulated/0/ etc.. part
        String emulatedGarbage = Environment.getExternalStorageDirectory().getAbsolutePath();

        if (newFolder == null) // If it is empty, set it to the default path.
            newFolder = context.getString(R.string.default_download_folder);

        if (newFolder.contains(emulatedGarbage))
            newFolder = newFolder.substring(emulatedGarbage.length() + 1);

        Config.DOWNLOAD_FOLDER = newFolder;

        editor.putString(context.getString(R.string.pref_download_location), HelperUtils.getAppDownloadDirectory().getPath());
        editor.apply();
    }

    public static File getAppDownloadDirectory() {
        File storage = Environment.getExternalStorageDirectory();
        File appStorage = new File(storage.getPath() + File.separator + Config.DOWNLOAD_FOLDER + File.separator);
        if (!appStorage.exists()) {
            appStorage.mkdirs();
            Timber.i("getAppDownloadDirectory: created gimmethefile dir");
        }
        return appStorage;
    }

    public static File getPathWithFilename(String filename) {
        return new File(getAppDownloadDirectory(), filename.replaceAll("[^ \\(\\)\\[\\]a-zA-Z0-9.-]", "_"));
    }
}
