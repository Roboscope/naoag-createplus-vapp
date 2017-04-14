package de.createplus.vertretungsplan.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Arrays;
import java.util.LinkedList;


/**
 * Created by Max Nuglisch on 04.02.2017.
 */

public class TgroupsDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "timetablegroups.db";
    private static final int DATABASE_VERSION = 10;

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TgroupsContract.TgroupsEntry.TABLE_NAME + " (" +
                    TgroupsContract.TgroupsEntry._ID + " INTEGER PRIMARY KEY," +
                    TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSEGROUP + " TEXT," +
                    TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSE + " TEXT," +
                    TgroupsContract.TgroupsEntry.COLUMN_NAME_ENABLED + " INTEGER)";

    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + TgroupsContract.TgroupsEntry.TABLE_NAME;

    private static final String SQL_DELETE_ENTRIES =
            "DELETE FROM " + TgroupsContract.TgroupsEntry.TABLE_NAME;

    public TgroupsDatabaseHelper(Context context) {
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

    public void addLine(String group, String course, boolean enabled) {
        int enabledInt = 0;
        if(enabled){
            enabledInt = 1;
        }
        ContentValues values = new ContentValues();

        values.put(TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSEGROUP, group);
        values.put(TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSE, course);
        values.put(TgroupsContract.TgroupsEntry.COLUMN_NAME_ENABLED, enabled);

        this.getWritableDatabase().insert(TgroupsContract.TgroupsEntry.TABLE_NAME, null, values);
    }

    public void removeAll() {
        this.getWritableDatabase().execSQL(SQL_DELETE_ENTRIES);
    }

    public String[] getCourse(String course){
        String[] projection = {
                TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSEGROUP,
                TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSE,
                TgroupsContract.TgroupsEntry.COLUMN_NAME_ENABLED
        };
        String selection = TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSE + " = ?";
        String[] selectionArgs = {course};

        String sortOrder =
                TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSEGROUP + " ASC";

        Cursor cursor = this.getReadableDatabase().query(
                TgroupsContract.TgroupsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        String[] tmp = new String[3];
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {

                tmp[0] = cursor.getString(cursor.getColumnIndexOrThrow(TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSEGROUP));
                tmp[1] = cursor.getString(cursor.getColumnIndexOrThrow(TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSE));
                tmp[2] = cursor.getString(cursor.getColumnIndexOrThrow(TgroupsContract.TgroupsEntry.COLUMN_NAME_ENABLED));
                Log.e("GROUPS", Arrays.toString(tmp));

            } while (cursor.moveToNext());
            cursor.close();
        }
        return tmp;
    }

    public LinkedList<String[]> getAllGroups()  {
        LinkedList<String[]> ret = new LinkedList<String[]>();

        String[] projection = {
                TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSEGROUP,
                TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSE,
                TgroupsContract.TgroupsEntry.COLUMN_NAME_ENABLED
        };

        String selection = TgroupsContract.TgroupsEntry._ID + " >= ?";
        String[] selectionArgs = {"0"};

        String sortOrder =
                TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSEGROUP + " ASC";

        Cursor cursor = this.getReadableDatabase().query(
                TgroupsContract.TgroupsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String[] tmp = new String[3];
                tmp[0] = cursor.getString(cursor.getColumnIndexOrThrow(TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSEGROUP));
                tmp[1] = cursor.getString(cursor.getColumnIndexOrThrow(TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSE));
                tmp[2] = cursor.getString(cursor.getColumnIndexOrThrow(TgroupsContract.TgroupsEntry.COLUMN_NAME_ENABLED));
                Log.e("GROUPS", Arrays.toString(tmp));
                ret.add(tmp);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return ret;
    }

    public String getEnabledCourseOfGroup(String group) {
        //Log.e("GROUPS", group);
        String[] projection = {
                TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSEGROUP,
                TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSE,
                TgroupsContract.TgroupsEntry.COLUMN_NAME_ENABLED
        };

        String selection = TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSEGROUP + " = ?";
        String[] selectionArgs = {group};

        String sortOrder =
                TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSEGROUP + " ASC";

        Cursor cursor = this.getReadableDatabase().query(
                TgroupsContract.TgroupsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String[] tmp = new String[3];
                tmp[0] = cursor.getString(cursor.getColumnIndexOrThrow(TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSEGROUP));
                tmp[1] = cursor.getString(cursor.getColumnIndexOrThrow(TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSE));
                tmp[2] = cursor.getString(cursor.getColumnIndexOrThrow(TgroupsContract.TgroupsEntry.COLUMN_NAME_ENABLED));
                //Log.e("GROUPS", Arrays.toString(tmp) + " ----------" +Integer.parseInt(tmp[2]));
                if(Integer.parseInt(tmp[2]) == 1){
                    Log.e(group, Arrays.toString(tmp));
                    return tmp[1];
                }

            } while (cursor.moveToNext());
            cursor.close();
        }
        return null;
    }

    public boolean doesExist(String group) {
        String[] projection = {
                TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSEGROUP,
                TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSE,
                TgroupsContract.TgroupsEntry.COLUMN_NAME_ENABLED
        };

        String selection = TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSEGROUP+ " = ?";
        String[] selectionArgs = {group};

        String sortOrder =
                TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSEGROUP + " ASC";

        Cursor cursor = this.getReadableDatabase().query(
                TgroupsContract.TgroupsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        boolean ret = false;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                ret = true;
            } while (cursor.moveToNext());
            cursor.close();
        }
        return ret;
    }

}
