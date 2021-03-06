package de.createplus.vertretungsplan.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.IdentityHashMap;

import de.createplus.vertretungsplan.MainActivity;
import de.createplus.vertretungsplan.backgroundservices.Pair;
import de.createplus.vertretungsplan.listview.Parent;


/**
 * Created by Max Nuglisch on 04.02.2017.
 */

public class ThtmlDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "timetablehtml.db";
    private static final int DATABASE_VERSION = 12;

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + ThtmlContract.ThtmlEntry.TABLE_NAME + " (" +
                    ThtmlContract.ThtmlEntry._ID + " INTEGER PRIMARY KEY," +
                    ThtmlContract.ThtmlEntry.COLUMN_NAME_CALENDARWEEK + " INTEGER," +
                    ThtmlContract.ThtmlEntry.COLUMN_NAME_CLASS + " TEXT," +
                    ThtmlContract.ThtmlEntry.COLUMN_NAME_HTML + " TEXT)";

    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + ThtmlContract.ThtmlEntry.TABLE_NAME;

    private static final String SQL_DELETE_ENTRIES =
            "DELETE FROM " + ThtmlContract.ThtmlEntry.TABLE_NAME;

    public ThtmlDatabaseHelper(Context context) {
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

    public void addLine(String week, String Class, String html) {
        ContentValues values = new ContentValues();

        values.put(ThtmlContract.ThtmlEntry.COLUMN_NAME_CALENDARWEEK, week);
        values.put(ThtmlContract.ThtmlEntry.COLUMN_NAME_CLASS, Class);
        values.put(ThtmlContract.ThtmlEntry.COLUMN_NAME_HTML, html);

        this.getWritableDatabase().insert(ThtmlContract.ThtmlEntry.TABLE_NAME, null, values);
    }

    public void removeAll() {
        this.getWritableDatabase().execSQL(SQL_DELETE_ENTRIES);
    }

    public void removeAnnotations() {

    }


    public String getHtml() {
        boolean A = false;
        if ((MainActivity.TomorrowWeek % 2) == 0) {
            A = true;
        }
        String[] projection = {
                ThtmlContract.ThtmlEntry.COLUMN_NAME_CALENDARWEEK,
                ThtmlContract.ThtmlEntry.COLUMN_NAME_CLASS,
                ThtmlContract.ThtmlEntry.COLUMN_NAME_HTML
        };

        String selection = ThtmlContract.ThtmlEntry.COLUMN_NAME_CALENDARWEEK + " >= ?";
        String[] selectionArgs = {"0"};

        String sortOrder =
                ThtmlContract.ThtmlEntry.COLUMN_NAME_CLASS + " ASC";

        Cursor cursor = this.getReadableDatabase().query(
                ThtmlContract.ThtmlEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        String result = "";
        boolean first = true;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                if (first) {
                    result += cursor.getString(cursor.getColumnIndexOrThrow(ThtmlContract.ThtmlEntry.COLUMN_NAME_HTML));
                    first = false;
                } else {
                    boolean lineisA = cursor.getInt(cursor.getColumnIndexOrThrow(ThtmlContract.ThtmlEntry.COLUMN_NAME_CALENDARWEEK)) % 2 == 0;
                    if ((lineisA && A) || (!lineisA && !A)) {
                        result = cursor.getString(cursor.getColumnIndexOrThrow(ThtmlContract.ThtmlEntry.COLUMN_NAME_HTML)) + "\n" + result;
                    }
                    if ((lineisA && !A) || (!lineisA && A)) {
                        result = result + "\n" + cursor.getString(cursor.getColumnIndexOrThrow(ThtmlContract.ThtmlEntry.COLUMN_NAME_HTML));
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return result;
    }

    public Pair getClassWeek() {
        String[] projection = {
                ThtmlContract.ThtmlEntry.COLUMN_NAME_CALENDARWEEK,
                ThtmlContract.ThtmlEntry.COLUMN_NAME_CLASS,
        };

        String selection = ThtmlContract.ThtmlEntry.COLUMN_NAME_CALENDARWEEK + " > ?";
        String[] selectionArgs = {"0"};

        String sortOrder =
                ThtmlContract.ThtmlEntry.COLUMN_NAME_CLASS + " ASC";

        Cursor cursor = this.getReadableDatabase().query(
                ThtmlContract.ThtmlEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        String[] a = new String[2];
        String[] b = new String[2];
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int i = 0;
            do {
                String x = cursor.getString(cursor.getColumnIndexOrThrow(ThtmlContract.ThtmlEntry.COLUMN_NAME_CALENDARWEEK));
                String y = cursor.getString(cursor.getColumnIndexOrThrow(ThtmlContract.ThtmlEntry.COLUMN_NAME_CLASS));

                if (i == 0 && Integer.parseInt(y) > 0) {
                    a[0] = x;
                    a[1] = y;
                } else if (i == 1 && Integer.parseInt(y) > 0) {
                    b[0] = x;
                    b[1] = y;
                }
                i++;

            } while (cursor.moveToNext());
            cursor.close();
        }
        return new Pair(a, b);
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
                CLASS = CLASSSPLIT[CLASSSPLIT.length - 1];
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
                    String out = "";
                    if (KIND.equals("Sondereins.")) {
                        out = TEXT + " " + " in der " + HOUR + " Stunde   Raum:" + NEWROOM + " SPLITPOINT " + "Keine weiteren Informationen vorhanden!";
                    } else {
                        out = COURSE + " " + KIND + " in der " + HOUR + " Stunde   Raum:" + ROOM + " -> " + NEWROOM + " SPLITPOINT " + TEXT;
                    }
                    arrayChildren.add(out);
                } else {
                    String out = "";
                    if (KIND.equals("Sondereins.")) {
                        out = TEXT + " " + " in der " + HOUR + " Stunde   Raum:" + NEWROOM + " SPLITPOINT " + "Keine weiteren Informationen vorhanden!";
                    } else {
                        out = COURSE + " " + KIND + " in der " + HOUR + " Stunde   Raum:" + ROOM + " -> " + NEWROOM + " SPLITPOINT " + TEXT;
                    }
                    arrayChildren.add(out);
                }
            } while (cursor.moveToNext());
            currentparent.setArrayChildren(arrayChildren);
            arrayParents.add(currentparent);

            cursor.close();
        }
        return arrayParents;
    }

    public String[] getHtmlArray() {
        String[] projection = {
                ThtmlContract.ThtmlEntry.COLUMN_NAME_CALENDARWEEK,
                ThtmlContract.ThtmlEntry.COLUMN_NAME_CLASS,
                ThtmlContract.ThtmlEntry.COLUMN_NAME_HTML
        };

        String selection = ThtmlContract.ThtmlEntry.COLUMN_NAME_CALENDARWEEK + " > ?";
        String[] selectionArgs = {"0"};

        String sortOrder =
                ThtmlContract.ThtmlEntry.COLUMN_NAME_CLASS + " ASC";

        Cursor cursor = this.getReadableDatabase().query(
                ThtmlContract.ThtmlEntry.TABLE_NAME,
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
                result += cursor.getString(cursor.getColumnIndexOrThrow(ThtmlContract.ThtmlEntry.COLUMN_NAME_HTML)) + "$$SPLITPOINT$$";
            } while (cursor.moveToNext());
            cursor.close();
        }
        return result.split("$$SPLITPOINT$$");
    }

    public String getSavedClass() {
        String[] projection = {
                ThtmlContract.ThtmlEntry.COLUMN_NAME_CALENDARWEEK,
                ThtmlContract.ThtmlEntry.COLUMN_NAME_CLASS,
                ThtmlContract.ThtmlEntry.COLUMN_NAME_HTML
        };

        String selection = ThtmlContract.ThtmlEntry._ID + " > ?";
        String[] selectionArgs = {"0"};

        String sortOrder =
                ThtmlContract.ThtmlEntry.COLUMN_NAME_CLASS + " ASC";

        Cursor cursor = this.getReadableDatabase().query(
                ThtmlContract.ThtmlEntry.TABLE_NAME,
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
                String[] a = cursor.getString(cursor.getColumnIndexOrThrow(ThtmlContract.ThtmlEntry.COLUMN_NAME_HTML)).split("<font size=\"7\" face=\"Arial\" color=\"#0000FF\">[AB]: ");
                if (a.length > 1) {
                    return a[1].split(" &nbsp;</font> ")[0];
                }

            } while (cursor.moveToNext());
        }
        return "";
    }
}
