package com.example.nick.droidar_tagit;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

/**
 * Created by nick on 1/10/2018.
 */
@Dao
public interface PlacedTagDao {

	@Query("select * from PlacedTag ORDER BY id DESC")
	LiveData<List<PlacedTag>> getPlacedTags();

	@Query("select * from PlacedTag where id = :id")
	PlacedTag getPlacedTagbyId(int id);

	@Insert(onConflict = REPLACE)
	void addPlacedTag(PlacedTag placedTag);

	@Delete
	void deletePlacedTag(PlacedTag placedTag);

	@Query("DELETE FROM PlacedTag")
	void deleteAllPlacedTags();

	@Query("DELETE FROM PlacedTag where id = :id")
	void deletePlacedTagById(int id);
}