package geyer.sensorlab.psychapp;

import android.provider.BaseColumns;

public class secureDatabaseContract {

        public secureDatabaseContract(){}

        public  static abstract class secureDatabase implements BaseColumns {
            public static final String TABLE_NAME = "events_table";
            public static final String COLUMN_NAME_ENTRY_ID = "column_id";
            public static final String EVENT = "event";
            public static final String TIMESTAMP = "timestamp";
        }
}
