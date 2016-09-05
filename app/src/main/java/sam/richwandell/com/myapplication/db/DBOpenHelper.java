package sam.richwandell.com.myapplication.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;

    public static final String DATABASE_NAME = "data.db";

    private static final String SCAN_TABLE_CREATE =
            "create table scan (s_id INTEGER PRIMARY KEY);";

    private static final String SCAN_RESULTS_TABLE_CREATE =
            "CREATE TABLE scan_results (" +
                    "s_id INTEGER, " +
                    "fp_id INTEGER, " +
                    "coords TEXT, " +
                    "value REAL, " +
                    "orig_values TEXT, " +
                    "PRIMARY KEY (s_id, fp_id)" +
                    ");";

    private static final String SCAN_RESULTS_DELETE = "drop table scan_results;";
    private static final String SCAN_DELETE = "drop table scan;";

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SCAN_TABLE_CREATE);
        db.execSQL(SCAN_RESULTS_TABLE_CREATE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SCAN_DELETE);
        db.execSQL(SCAN_RESULTS_DELETE);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
