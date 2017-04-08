package de.createplus.vertretungsplan.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import de.createplus.vertretungsplan.listview.Parent;


/**
 * Created by Max Nuglisch on 04.02.2017.
 */

public class TplanDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "timetablecourses.db";
    private static final int DATABASE_VERSION = 5;

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TplanContract.TplanEntry.TABLE_NAME + " (" +
                    TplanContract.TplanEntry._ID + " INTEGER PRIMARY KEY," +
                    TplanContract.TplanEntry.COLUMN_NAME_WEEK + " INTEGER," +
                    TplanContract.TplanEntry.COLUMN_NAME_DAY + " INTEGER," +
                    TplanContract.TplanEntry.COLUMN_NAME_HOUR + " INTEGER," +
                    TplanContract.TplanEntry.COLUMN_NAME_COURSEGROUP + " TEXT)";

    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + TplanContract.TplanEntry.TABLE_NAME;

    private static final String SQL_DELETE_ENTRIES =
            "DELETE FROM " + TplanContract.TplanEntry.TABLE_NAME;

    public TplanDatabaseHelper(Context context) {
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

    public void addLine(int week, int day, int hour, String group) {
        ContentValues values = new ContentValues();

        values.put(TplanContract.TplanEntry.COLUMN_NAME_WEEK, week);
        values.put(TplanContract.TplanEntry.COLUMN_NAME_DAY, day);
        values.put(TplanContract.TplanEntry.COLUMN_NAME_HOUR, hour);
        values.put(TplanContract.TplanEntry.COLUMN_NAME_COURSEGROUP, group);

        this.getWritableDatabase().insert(TplanContract.TplanEntry.TABLE_NAME, null, values);
    }

    public void removeAll() {
        this.getWritableDatabase().execSQL(SQL_DELETE_ENTRIES);
    }

    public String getPlan(int week) {
        String[] projection = {
                TplanContract.TplanEntry.COLUMN_NAME_WEEK,
                        TplanContract.TplanEntry.COLUMN_NAME_DAY,
                        TplanContract.TplanEntry.COLUMN_NAME_HOUR,
                        TplanContract.TplanEntry.COLUMN_NAME_COURSEGROUP
        };

        String selection = TplanContract.TplanEntry.COLUMN_NAME_WEEK + " = ?";
        String[] selectionArgs = {""+week};

        String sortOrder =
                TplanContract.TplanEntry.COLUMN_NAME_DAY + " ASC";

        Cursor cursor = this.getReadableDatabase().query(
                TplanContract.TplanEntry.TABLE_NAME,
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
                result += cursor.getString(cursor.getColumnIndexOrThrow(TplanContract.TplanEntry.COLUMN_NAME_COURSEGROUP)) + "\n";
            } while (cursor.moveToNext());
            cursor.close();
        }
        return result;
    }

}
