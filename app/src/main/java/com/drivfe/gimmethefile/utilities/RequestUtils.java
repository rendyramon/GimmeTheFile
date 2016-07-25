package com.drivfe.gimmethefile.utilities;

import com.drivfe.gimmethefile.errors.BaseException;
import com.drivfe.gimmethefile.errors.ExtractorNotSupportedException;
import com.drivfe.gimmethefile.errors.FailedToConnectException;
import com.drivfe.gimmethefile.errors.HTTPErrorException;
import com.drivfe.gimmethefile.errors.OtherException;
import com.drivfe.gimmethefile.models.MediaFileBucket;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class RequestUtils {
    public static MediaFileBucket requestJson(URL url) throws BaseException {
        //TODO: response should always be a JSON
        Gson gson = new Gson();
        MediaFileBucket response;
        Response httpResponse;
        String data;

        try {
            httpResponse = simpleHttpRequest(url);
            data = httpResponse.body().string();
        } catch (IOException e) {
            Timber.d(e, "requestJson");
            throw new FailedToConnectException("Request took more than 15 seconds. Website may be offline", -1);
        }

        if (!data.isEmpty() && httpResponse.header("Content-Type", "").equals("application/json")) {
            response = gson.fromJson(data, MediaFileBucket.class);
            response = HelperUtils.refactorBucket(response);
        } else {
            Timber.d("requestJson(URL): Content-Type not application/json");
            if (data.contains("ERROR: "))
                data = data.substring("ERROR: ".length());

            if (data.contains("; please report this issue on https"))
                data = data.substring(0, data.indexOf("; please report this issue"));

            if (data.contains("Extractor not supported") || data.contains("Unsupported URL")) {
                throw new ExtractorNotSupportedException("This website may not be supported",
                        "Not all websites are supported by this application and youtube-dl.",
                        httpResponse.code());
            }
            if (data.toLowerCase().contains("httperror")) {
                throw new HTTPErrorException("There was an error retrieving information from the website.",
                        data,
                        httpResponse.code());
            }
            throw new OtherException("An error has occurred.", data, -1);
        }

        return response;
    }

    public static Response simpleHttpRequest(URL url) throws IOException {
        Response resp = null;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();
        resp = client.newCall(request).execute();

        return resp;
    }
}
