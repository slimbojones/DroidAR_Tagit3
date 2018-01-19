package com.example.nick.droidar_tagit;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by slimbojones on 1/6/2018.
 */
@Entity
public class PlacedTag {

    //TODO convert some of these strings to doubles

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int userid;
    public String bitmapString;
    public String tagTypeString;
    public double longString;
    public double latString;
    public double altString;
    //@TypeConverters(DateConverter.class)
    //private Date borrowDate;

    public PlacedTag(String bitmapString, String tagTypeString, double longString, double latString, double altString) {
        this.bitmapString = bitmapString;
        this.tagTypeString = tagTypeString;
        this.longString = longString;
        this.latString = latString;
        this.altString = altString;
        this.userid = userid;
    }

    public String getbitmapString() {
        return bitmapString;
    }
    public String gettagTypeString() {
        return tagTypeString;
    }
    public double getlongString() {
        return longString;
    }
    public double getlatString() {
        return latString;
    }
    public double getaltString() {
        return altString;
    }
    public int getuserid() {
        return userid;
    }
    public int getid() {
        return id;
    }

    public void setbitmapString(String bitmapString){ this.bitmapString = bitmapString; }
    public void settagTypeString(String tagTypeString){ this.tagTypeString = tagTypeString; }
    public void setlongString(double longString){ this.longString = longString; }
    public void setlatString(double latString){ this.latString = latString; }
    public void setaltString(double altString){ this.altString = altString; }

}
