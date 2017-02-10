package de.createplus.vertretungsplan.databases;

import android.provider.BaseColumns;

/**
 * TODO: MISSING JAVADOC
 *
 * @author Roboscope
 * @version 1.0
 * @since 2017-02-10
 */
public class SPContract {


    private SPContract(){}

    /* Inner class that defines the table contents */
    public static class SPEntry implements BaseColumns {
        public static final String TABLE_NAME = "substitutionplan";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_HOUR = "hour";
        public static final String COLUMN_NAME_ROOM = "room";
        public static final String COLUMN_NAME_NEWROOM = "newroom";
        public static final String COLUMN_NAME_KIND = "kind";
        public static final String COLUMN_NAME_TEXT = "text";
        public static final String COLUMN_NAME_CLASS = "class";
        public static final String COLUMN_NAME_COURSE = "course";
        public static final String COLUMN_NAME_TEACHER = "teacher";
    }
}