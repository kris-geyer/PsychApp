package geyer.sensorlab.psychapp;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

public class secureDatabaseDbHelper extends SQLiteOpenHelper {
    private static secureDatabaseDbHelper instance;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FeedReader.db";

    private static final String INTEGER_TYPE = " INTEGER";
    private static final String TEXT_TYPE = " TEXT";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + secureDatabaseContract.secureDatabase.TABLE_NAME + " (" +
                    secureDatabaseContract.secureDatabase.COLUMN_NAME_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    secureDatabaseContract.secureDatabase.EVENT + TEXT_TYPE + "," +
                    secureDatabaseContract.secureDatabase.TIMESTAMP + INTEGER_TYPE +
                    " )";


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + secureDatabaseContract.secureDatabase.TABLE_NAME;

    public secureDatabaseDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static public synchronized secureDatabaseDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new secureDatabaseDbHelper(context);
        }
        return instance;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
