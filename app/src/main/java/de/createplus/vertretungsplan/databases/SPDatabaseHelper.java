package de.createplus.vertretungsplan.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import de.createplus.vertretungsplan.listview.Parent;


/**
 * Created by maxnu on 04.02.2017.
 */

public class SPDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "substitutionplan.db";
    private static final int DATABASE_VERSION = 4;

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + SPContract.SPEntry.TABLE_NAME + " (" +
                    SPContract.SPEntry._ID + " INTEGER PRIMARY KEY," +
                    SPContract.SPEntry.COLUMN_NAME_DATE + " DATE," +
                    SPContract.SPEntry.COLUMN_NAME_HOUR + " TEXT," +
                    SPContract.SPEntry.COLUMN_NAME_ROOM + " TEXT," +
                    SPContract.SPEntry.COLUMN_NAME_NEWROOM + " TEXT," +
                    SPContract.SPEntry.COLUMN_NAME_KIND + " TEXT," +
                    SPContract.SPEntry.COLUMN_NAME_TEXT + " TEXT," +
                    SPContract.SPEntry.COLUMN_NAME_CLASS + " TEXT," +
                    SPContract.SPEntry.COLUMN_NAME_COURSE + " TEXT," +
                    SPContract.SPEntry.COLUMN_NAME_PLANINFO + " TEXT" +
                    ")";

    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + SPContract.SPEntry.TABLE_NAME;

    private static final String SQL_DELETE_ENTRIES =
            "DELETE FROM " + SPContract.SPEntry.TABLE_NAME;

    public SPDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addLine(String title, String[] data, String info) {
        ContentValues values = new ContentValues();
        values.put(SPContract.SPEntry.COLUMN_NAME_DATE, data[0] + "2017");//TODO: DATUM MACHEN
        values.put(SPContract.SPEntry.COLUMN_NAME_HOUR, data[1]);
        values.put(SPContract.SPEntry.COLUMN_NAME_ROOM, data[4]);
        values.put(SPContract.SPEntry.COLUMN_NAME_NEWROOM, data[5]);
        values.put(SPContract.SPEntry.COLUMN_NAME_KIND, data[3]);
        values.put(SPContract.SPEntry.COLUMN_NAME_TEXT, data[6]);
        values.put(SPContract.SPEntry.COLUMN_NAME_CLASS, title);
        values.put(SPContract.SPEntry.COLUMN_NAME_COURSE, data[2]);
        values.put(SPContract.SPEntry.COLUMN_NAME_PLANINFO, info);

        this.getWritableDatabase().insert(SPContract.SPEntry.TABLE_NAME, null, values);
    }

    public void removeAll() {
        this.getWritableDatabase().execSQL(SQL_DELETE_ENTRIES);
    }

    public String test() {
        String[] projection = {
                SPContract.SPEntry.COLUMN_NAME_CLASS,
                SPContract.SPEntry.COLUMN_NAME_HOUR,
                SPContract.SPEntry.COLUMN_NAME_COURSE,
                SPContract.SPEntry.COLUMN_NAME_KIND,
                SPContract.SPEntry.COLUMN_NAME_ROOM
        };

        String selection = SPContract.SPEntry.COLUMN_NAME_DATE + " > ?";
        String[] selectionArgs = {"0"};

        String sortOrder =
                SPContract.SPEntry.COLUMN_NAME_CLASS + " ASC";

        Cursor cursor = this.getReadableDatabase().query(
                SPContract.SPEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        String result = "";
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                result += cursor.getString(cursor.getColumnIndexOrThrow(SPContract.SPEntry.COLUMN_NAME_CLASS)) + " | " + cursor.getString(cursor.getColumnIndexOrThrow(SPContract.SPEntry.COLUMN_NAME_HOUR)) + " | " + cursor.getString(cursor.getColumnIndexOrThrow(SPContract.SPEntry.COLUMN_NAME_COURSE)) + " | " + cursor.getString(cursor.getColumnIndexOrThrow(SPContract.SPEntry.COLUMN_NAME_KIND)) + " | " + cursor.getString(cursor.getColumnIndexOrThrow(SPContract.SPEntry.COLUMN_NAME_ROOM)) + "\n";
            } while (cursor.moveToNext());
            cursor.close();
        }
        return result;
    }


    public ArrayList<Parent> getPlan(String date) {
        String[] projection = {
                SPContract.SPEntry.COLUMN_NAME_CLASS,
                SPContract.SPEntry.COLUMN_NAME_HOUR,
                SPContract.SPEntry.COLUMN_NAME_COURSE,
                SPContract.SPEntry.COLUMN_NAME_KIND,
                SPContract.SPEntry.COLUMN_NAME_ROOM,
                SPContract.SPEntry.COLUMN_NAME_DATE,
                SPContract.SPEntry.COLUMN_NAME_NEWROOM,
                SPContract.SPEntry.COLUMN_NAME_TEXT,
                SPContract.SPEntry.COLUMN_NAME_PLANINFO,
        };

        //String selection = SPContract.SPEntry._ID + " > ?";
        //String[] selectionArgs = {"0"};
        String selection = SPContract.SPEntry.COLUMN_NAME_DATE + " = ?";
        String[] selectionArgs = {date};

        //Log.e("SQL", date);

        String sortOrder =
                SPContract.SPEntry.COLUMN_NAME_CLASS + " ASC";

        Cursor cursor = this.getReadableDatabase().query(
                SPContract.SPEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        ArrayList<Parent> arrayParents = new ArrayList<Parent>();
        //ArrayList<String> arrayChildren = new ArrayList<String>();
        ArrayList<String> arrayChildren = null;
        Parent currentparent = null;

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {

                String CLASS = cursor.getString(cursor.getColumnIndexOrThrow(SPContract.SPEntry.COLUMN_NAME_CLASS)),
                        HOUR = cursor.getString(cursor.getColumnIndexOrThrow(SPContract.SPEntry.COLUMN_NAME_HOUR)),
                        COURSE = cursor.getString(cursor.getColumnIndexOrThrow(SPContract.SPEntry.COLUMN_NAME_COURSE)),
                        KIND = cursor.getString(cursor.getColumnIndexOrThrow(SPContract.SPEntry.COLUMN_NAME_KIND)),
                        ROOM = cursor.getString(cursor.getColumnIndexOrThrow(SPContract.SPEntry.COLUMN_NAME_ROOM)),
                        DATE = cursor.getString(cursor.getColumnIndexOrThrow(SPContract.SPEntry.COLUMN_NAME_DATE)),
                        NEWROOM = cursor.getString(cursor.getColumnIndexOrThrow(SPContract.SPEntry.COLUMN_NAME_NEWROOM)),
                        TEXT = cursor.getString(cursor.getColumnIndexOrThrow(SPContract.SPEntry.COLUMN_NAME_TEXT)),
                        PLANINFO = cursor.getString(cursor.getColumnIndexOrThrow(SPContract.SPEntry.COLUMN_NAME_PLANINFO));
                String[] CLASSSPLIT = CLASS.split(" ");
                CLASS = CLASSSPLIT[CLASSSPLIT.length-1];
                //Log.e("VERTRETUNGSPLAN", ""+DATE);
                //Log.e("VERTRETUNGSPLANPARENT", currentparent.getTitle());
                if (currentparent == null) {
                    currentparent = new Parent();
                    currentparent.setTitle(CLASS);
                    arrayChildren = new ArrayList<String>();
                }
                if (!currentparent.getTitle().equals(CLASS)) {
                    //Log.e("VERTRETUNGSPLAN", "DOIN");
                    currentparent.setArrayChildren(arrayChildren);
                    arrayChildren = new ArrayList<String>();
                    if (!currentparent.getTitle().equals("-----") && !currentparent.getTitle().equals("Konferenztag") && !currentparent.getTitle().equals("Pause")) {
                        arrayParents.add(currentparent);
                        //Log.e("SP DATABASE", currentparent.getTitle());
                    }

                    currentparent = new Parent();
                    currentparent.setTitle(CLASS);
                    arrayChildren.add(COURSE + " " + KIND + " in der " + HOUR + " Stunde   Raum:" + ROOM + " -> " + NEWROOM + " SPLITPOINT " + TEXT);
                } else {
                    arrayChildren.add(COURSE + " " + KIND + " in der " + HOUR + " Stunde   Raum:" + ROOM + " -> " + NEWROOM + " SPLITPOINT " + TEXT);
                }
            } while (cursor.moveToNext());
            currentparent.setArrayChildren(arrayChildren);
            arrayParents.add(currentparent);

            cursor.close();
        }
        return arrayParents;
    }

}
