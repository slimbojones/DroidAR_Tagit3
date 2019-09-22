package com.example.nick.droidar_tagit;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

//See build gradle to use exportedSchema or set to false
@Database(entities = {Tagpost.class, PlacedTag.class}, version = 1, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {

	private static AppDatabase INSTANCE;

	static AppDatabase getDatabase(Context context) {
		if (INSTANCE == null) {
			INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "tagpost_db16").build();
		}
		return INSTANCE;
	}

	public abstract TagpostDao itemAndPersonModel();
	public abstract PlacedTagDao placedTagModel();
}
