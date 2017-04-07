package de.createplus.vertretungsplan.databases;

import android.provider.BaseColumns;

/**
 * TODO: MISSING JAVADOC
 *
 * @author Roboscope
 * @version 1.0
 * @since 2017-02-10
 */
public class TgroupsContract {


    private TgroupsContract(){}

    /* Inner class that defines the table contents */
    public static class TgroupsEntry implements BaseColumns {
        public static final String TABLE_NAME = "timetablegroups";
        public static final String COLUMN_NAME_COURSEGROUP = "coursegroup";
        public static final String COLUMN_NAME_COURSE = "course";
        public static final String COLUMN_NAME_ENABLED = "enabled";
    }
}