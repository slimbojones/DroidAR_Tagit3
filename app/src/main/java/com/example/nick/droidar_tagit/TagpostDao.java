package com.example.nick.droidar_tagit;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

/**
 * Created by slimbojones on 1/6/2018.
 */
@Dao
public interface TagpostDao {

	@Query("select * from Tagpost ORDER BY id DESC")
	LiveData<List<Tagpost>> getAllTagposts();

	@Query("select * from Tagpost where id = :id")
	Tagpost getTagpostbyId(String id);

	@Insert(onConflict = REPLACE)
	void addTagpost(com.example.nick.droidar_tagit.Tagpost tagpost);

	@Delete
	void deleteTagpost(com.example.nick.droidar_tagit.Tagpost tagpost);

	@Query("DELETE FROM Tagpost")
	void deleteAllTagposts();
}


