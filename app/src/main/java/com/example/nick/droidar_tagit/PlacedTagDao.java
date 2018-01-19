package com.example.nick.droidar_tagit;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.util.Log;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

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
    void addPlacedTag(com.example.nick.droidar_tagit.PlacedTag placedTag);

    @Delete
    void deletePlacedTag(com.example.nick.droidar_tagit.PlacedTag placedTag);

    @Query("DELETE FROM PlacedTag")
    public void deleteAllPlacedTags();

    @Query("DELETE FROM PlacedTag where id = :id")
    public void deletePlacedTagById(int id);

}

