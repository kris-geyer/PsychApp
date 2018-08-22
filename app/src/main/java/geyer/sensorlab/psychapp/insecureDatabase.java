package geyer.sensorlab.psychapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class insecureDatabase {

        private final static String keyRowId = "_id";
        private final static String entry = "entry";
        private final static String Timestamp = "timeStamp";
        private final static String databaseName = "insecureData";
        private final static String databaseTable = "errorDataTable";
        private final static int databaseVersion = 1;
        private insecureDatabase.DbHelper databaseHelper;
        private SQLiteDatabase errorDatabaseSQL;

        private final Context context;

        public insecureDatabase(Context context) {
            this.context = context;
        }

        public long addEntry(String errorInput, long time) {
            Log.i("database", "added: " + errorInput + " - " + time);
            ContentValues contentValues = new ContentValues();
            contentValues.put(entry, errorInput);
            contentValues.put(Timestamp, time);
            return errorDatabaseSQL.insert(databaseTable, null, contentValues);
        }

        public ArrayList<String> getLoggedEntries() {
            String[] columns = new String[]{keyRowId, entry, Timestamp};
            Cursor c = errorDatabaseSQL.query(databaseTable, columns, null, null, null, null, null);
            int iError = c.getColumnIndex(entry);
            ArrayList<String> databaseError = new ArrayList<String>();
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                databaseError.add(c.getString(iError));
            }

            return databaseError;
        }
        public ArrayList<Long> getTimes() {
            String [] columns = new String[]{keyRowId, entry, Timestamp};
            Cursor c = errorDatabaseSQL.query(databaseTable, columns, null, null, null, null, null);
            int iTime = c.getColumnIndex(Timestamp);
            ArrayList<Long> databaseTime = new ArrayList<Long>();
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
                databaseTime.add(c.getLong(iTime));
            }
            return databaseTime;
        }

        private class DbHelper extends SQLiteOpenHelper {

            public DbHelper(Context context) {
                super(context, databaseName, null, databaseVersion);
            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE " + databaseTable + " ("
                        + keyRowId + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + entry + " TEXT,"
                        + Timestamp + " INTEGER);"
                );
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                db.execSQL("DROP TABLE IF EXISTS " + databaseTable);
                onCreate(db);
            }
        }

        public insecureDatabase open() throws SQLException {
            databaseHelper = new insecureDatabase.DbHelper(context);
            errorDatabaseSQL = databaseHelper.getWritableDatabase();
            return this;
        }

        public void close() {
            databaseHelper.close();
        }
    }
