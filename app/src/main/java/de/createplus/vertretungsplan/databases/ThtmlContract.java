package de.createplus.vertretungsplan.databases;

import android.provider.BaseColumns;

/**
 * TODO: MISSING JAVADOC
 *
 * @author Roboscope
 * @version 1.0
 * @since 2017-02-10
 */
public class ThtmlContract {


    private ThtmlContract(){}

    /* Inner class that defines the table contents */
    public static class ThtmlEntry implements BaseColumns {
        public static final String TABLE_NAME = "timetablehtml";
        public static final String COLUMN_NAME_CALENDARWEEK = "calendarweek";
        public static final String COLUMN_NAME_HTML = "html";
        public static final String COLUMN_NAME_CLASS = "class";
    }
}