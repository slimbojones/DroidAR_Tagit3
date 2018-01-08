package com.example.nick.droidar_tagit;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


/**
 * Created by slimbojones on 1/6/2018.
 */

@Entity
public class Tagpost {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int userid;
    public String itemString;
    public String tagType;
    //@TypeConverters(DateConverter.class)
    //private Date borrowDate;


    public Tagpost(String itemString, String tagType, int userid) {
        this.itemString = itemString;
        this.tagType = tagType;
        this.userid = userid;
    }

    public String getitemString() {
        return itemString;
    }

    public void setItemString(String itemString){ this.itemString = itemString; }

    public String gettagType() {
        return tagType;
    }
    public int getuserid() {
        return userid;
    }

}
