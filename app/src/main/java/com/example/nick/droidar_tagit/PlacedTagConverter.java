package com.example.nick.droidar_tagit;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by nick on 1/11/2018.
 */

public class PlacedTagConverter {

	//String to ArrayList
	@TypeConverter
	public static ArrayList<PlacedTag> fromString(String value) {
		Type listType = new TypeToken<ArrayList<PlacedTag>>() {}.getType();
		return new Gson().fromJson(value, listType);
	}

	//ArrayList to String
	@TypeConverter
	public static String fromArrayLisr(ArrayList<PlacedTag> list) {
		Gson gson = new Gson();
		String json = gson.toJson(list);
		return json;
	}
}
