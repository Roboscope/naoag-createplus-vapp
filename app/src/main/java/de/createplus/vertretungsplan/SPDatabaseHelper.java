package de.createplus.vertretungsplan;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by maxnu on 04.02.2017.
 */

public class SPDatabaseHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "substitutionplan.db";
    private static final int DATABASE_VERSION = 1;

    public SPDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("some sql statement to create table");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("some sql statement to do something");
    }
}
