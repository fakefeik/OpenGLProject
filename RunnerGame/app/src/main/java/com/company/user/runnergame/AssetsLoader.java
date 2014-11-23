package com.company.user.runnergame;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Admin on 23.11.2014.
 */
public class AssetsLoader {
    public static String loadAsset(Context context, String filename) {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream is = assetManager.open(filename);
            StringBuilder stringBuilder = new StringBuilder();
            int chr;
            while ((chr = is.read()) != -1) {
                stringBuilder.append((char) chr);
            }
            is.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
