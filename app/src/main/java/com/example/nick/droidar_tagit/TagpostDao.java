package com.example.nick.droidar_tagit;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

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
    public void deleteAllTagposts();

}


