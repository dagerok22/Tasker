package com.sergey_suslov.tasker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by PMI51 on 03.02.2017.
 */

public final class FeedReaderContract {
    public FeedReaderContract() {}

    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "tasks_main";
        public static final String COLUMN_NAME_TASK_ID = "task_id";
        public static final String COLUMN_NAME_TITLE = "task_title";
        public static final String COLUMN_NAME_DATE = "task_date";
        public static final String COLUMN_NAME_STATUS = "task_status";
        public static final String COLUMN_NAME_PRIORITY = "task_uistatus";
    }


}
