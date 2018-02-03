package com.example.nick.droidar_tagit;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Data model for the results coming from Google Image Search API
 *
 */
public class ImageResult implements Serializable {
    // TODO
    private static final long serialVersionUID = 111111111;

    private String fullUrl;
    private String thumbUrl;

    public ImageResult(JSONObject json) {
        try {

            this.fullUrl = (String) json.get("link");
            this.thumbUrl= (String) json.getJSONObject("image").get("thumbnailLink");

        } catch (JSONException e) {
            this.fullUrl = null;
            this.thumbUrl = null;
        }
    }

    public String getFullUrl() {
        return fullUrl;
    }
    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }
    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String toString() {
        return this.thumbUrl;
    }

    public static ArrayList <ImageResult> fromJSONArray(
            JSONArray array) {

        ArrayList<ImageResult> results = new ArrayList<ImageResult>();
        for (int x = 0; x < array.length(); x++) {
            try {

                //JSONObject jsonPagemap = array.getJSONObject(x).getJSONObject("pagemap");
                //JSONObject cse_thumbnail = (JSONObject) jsonPagemap.getJSONArray("cse_thumbnail").get(0);
                //JSONObject cse_image = (JSONObject) jsonPagemap.getJSONArray("cse_image").get(0);

                String fullUrl = (String) array.getJSONObject(x).get("link");
                String thumbUrl = (String) array.getJSONObject(x).getJSONObject("image").get("thumbnailLink");

                //String fullUrl = (String) cse_image.get("src");
                //String thumbUrl= (String) cse_thumbnail.get("src");

                if(fullUrl != null || thumbUrl != null || !fullUrl.isEmpty() || !thumbUrl.isEmpty()){
                    results.add(new ImageResult(array.getJSONObject(x)));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return results;
    }
}