package de.createplus.vertretungsplan.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by maxnu on 04.02.2017.
 */

public class SPDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "substitutionplan.db";
    private static final int DATABASE_VERSION = 1;

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
                    SPContract.SPEntry.COLUMN_NAME_TEACHER + " TEXT" +
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

    public void addLine(String title, String[] data) {
        ContentValues values = new ContentValues();
        values.put(SPContract.SPEntry.COLUMN_NAME_DATE, title);
        values.put(SPContract.SPEntry.COLUMN_NAME_HOUR, data[0] + "2017");//TODO: DATUM MACHEN JO SPLITTEN
        values.put(SPContract.SPEntry.COLUMN_NAME_ROOM, data[1]);
        values.put(SPContract.SPEntry.COLUMN_NAME_NEWROOM, data[2]);
        values.put(SPContract.SPEntry.COLUMN_NAME_KIND, data[3]);
        values.put(SPContract.SPEntry.COLUMN_NAME_TEXT, data[4]);
        values.put(SPContract.SPEntry.COLUMN_NAME_CLASS, data[5]);
        values.put(SPContract.SPEntry.COLUMN_NAME_COURSE, data[6]);
        values.put(SPContract.SPEntry.COLUMN_NAME_TEACHER, "Es gibt kein index 7");

        this.getWritableDatabase().insert(SPContract.SPEntry.TABLE_NAME, null, values);
    }

    public void removeAll() {
        this.getWritableDatabase().execSQL(SQL_DELETE_ENTRIES);
    }

    public String test() {
        String[] projection = {
                SPContract.SPEntry.COLUMN_NAME_CLASS
        };

        String selection = SPContract.SPEntry._ID + " > ?";
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
                result += cursor.getString(cursor.getColumnIndexOrThrow(SPContract.SPEntry.COLUMN_NAME_CLASS)) + "\n";
            } while (cursor.moveToNext());
            cursor.close();
        }
        return result;
    }
}
