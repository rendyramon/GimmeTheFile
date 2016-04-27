package com.drivfe.gimmethefile.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.drivfe.gimmethefile.BuildConfig;
import com.drivfe.gimmethefile.R;

import java.util.LinkedHashMap;
import java.util.Map;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(createAboutView());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private View createAboutView() {
        AboutPage page = new AboutPage(this);
        Element version = new Element(null, String.format("Version %s", BuildConfig.VERSION_NAME), null);
        Element supportedSites = new Element(null, "Supported sites", null);
        supportedSites.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("https://rg3.github.io/youtube-dl/supportedsites.html")));
        Element youtubeDL = new Element(null, "Youtube-DL", null);
        youtubeDL.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("https://rg3.github.io/youtube-dl")));
        Element github = new Element(null, "Find us on Github", null);
        github.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/drivfe/GimmeTheFile")));

        page.setImage(R.mipmap.ic_launcher)
                .setDescription("Share any link with this app and download video or music of various formats and sizes.")
                .addItem(version)
                .addItem(supportedSites)
                .addItem(youtubeDL)
                .addItem(github)
                .addGroup("Open Source Libraries");

        LinkedHashMap<String, String> librariesList = new LinkedHashMap<>();
        librariesList.put("Picasso", "https://github.com/square/picasso");
        librariesList.put("Gson", "https://github.com/google/gson");
        librariesList.put("RxJava", "https://github.com/ReactiveX/RxJava");
        librariesList.put("RxAndroid", "https://github.com/ReactiveX/RxAndroid");
        librariesList.put("RxPermissions", "https://github.com/tbruyelle/RxPermissions");
        librariesList.put("Material Dialogs", "https://github.com/afollestad/material-dialogs");
        librariesList.put("Android About Page", "https://github.com/medyo/android-about-page");
        librariesList.put("Timber", "https://github.com/JakeWharton/timber");
        librariesList.put("Butterknife", "https://github.com/JakeWharton/butterknife");
        librariesList.put("OkHttp", "https://github.com/square/okhttp");

        for (Map.Entry<String, String> lib : librariesList.entrySet()) {
            Element libElement = new Element();
            libElement.setTitle(lib.getKey());

            Intent libIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(lib.getValue()));
            libElement.setIntent(libIntent);

            page.addItem(libElement);
        }

        // Ugly stuff to get the page to work with the appbar
        CoordinatorLayout about = (CoordinatorLayout) getLayoutInflater().inflate(R.layout.activity_about, null);
        ScrollView aboutScroll = (ScrollView) about.findViewById(R.id.about_scroll_view);
        ScrollView ppp = (ScrollView) page.create();
        LinearLayout ll = (LinearLayout) ppp.getChildAt(0);
        ppp.removeView(ll);

        aboutScroll.addView(ll);

        return about;
    }
}
