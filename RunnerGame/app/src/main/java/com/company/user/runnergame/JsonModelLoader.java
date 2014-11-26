package com.company.user.runnergame;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Admin on 23.11.2014.
 */
public class JsonModelLoader {
    public static Mesh load(Context context, String filename) throws JSONException {
        JSONObject object = new JSONObject(AssetsLoader.loadString(context, filename));
        JSONArray verticesJson = object.getJSONArray("vertices");
        JSONArray normalsJson = object.getJSONArray("normals");
        JSONArray texturesJson = object.getJSONArray("textures");
        JSONArray indicesJson = object.getJSONArray("indices");
        JSONArray positionJson = object.getJSONArray("position");
        JSONArray rotationJson = object.getJSONArray("rotation");
        JSONArray scalingJson = object.getJSONArray("scaling");
        String name = object.getString("name");
        return new Mesh(name, getFloatArray(positionJson), getFloatArray(rotationJson), getFloatArray(scalingJson),
                getFloatArray(verticesJson), getFloatArray(normalsJson), getFloatArray(texturesJson), getIntArray(indicesJson));
    }

    private static float[] getFloatArray(JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        float[] array = new float[length];
        for (int i = 0; i < length; i++) {
            array[i] = (float)jsonArray.getDouble(i);
        }
        return array;
    }

    private static int[] getIntArray(JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = jsonArray.getInt(i);
        }
        return array;
    }
}
