package com.example.nfkatprototype;

import android.graphics.Bitmap;
import android.os.StrictMode;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class NFTsManager {

    public static String getResponseText(String urlString) throws IOException {
        StringBuilder response = new StringBuilder();
        URL url = new URL(urlString);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection)url.openConnection();
        if (httpsURLConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
            BufferedReader input = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
            String strLine;
            while ((strLine = input.readLine()) != null) {
                response.append(strLine);
            }
            input.close();
        }
        return response.toString();
    }

    //For Image Size 640*480, use MAX_SIZE =  307200 as 640*480 307200
    private final static long MAX_SIZE = 160000;

    public static Bitmap reduceBitmapSize(@NonNull Bitmap bitmap) {
        try {
            double ratioSquare;
            int bitmapHeight, bitmapWidth;
            bitmapHeight = bitmap.getHeight();
            bitmapWidth = bitmap.getWidth();
            ratioSquare = (double) (bitmapHeight * bitmapWidth) / (double) MAX_SIZE;
            if (ratioSquare <= 1)
                return bitmap;
            double ratio = Math.sqrt(ratioSquare);
            int requiredHeight = (int) Math.round(bitmapHeight / ratio);
            int requiredWidth = (int) Math.round(bitmapWidth / ratio);
            return Bitmap.createScaledBitmap(bitmap, requiredWidth, requiredHeight, true);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
