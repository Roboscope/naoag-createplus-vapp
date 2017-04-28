package de.createplus.vertretungsplan.databases;

import android.provider.BaseColumns;


public class TgroupsContract {


    private TgroupsContract(){}

    /* Inner class that defines the table contents */
    public static class TgroupsEntry implements BaseColumns {
        public static final String TABLE_NAME = "timetablegroups";
        public static final String COLUMN_NAME_COURSE = "course";
        public static final String COLUMN_NAME_ENABLED = "enabled";
    }
}