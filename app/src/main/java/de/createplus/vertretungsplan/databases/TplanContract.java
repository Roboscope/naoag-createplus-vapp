package de.createplus.vertretungsplan.databases;

import android.provider.BaseColumns;


public class TplanContract {


    private TplanContract(){}

    /* Inner class that defines the table contents */
    public static class TplanEntry implements BaseColumns {
        public static final String TABLE_NAME = "timetableplan";
        public static final String COLUMN_NAME_WEEK = "week";
        public static final String COLUMN_NAME_DAY = "day";
        public static final String COLUMN_NAME_HOUR = "hour";
        public static final String COLUMN_NAME_COURSEGROUP  = "coursegroup";
    }
}