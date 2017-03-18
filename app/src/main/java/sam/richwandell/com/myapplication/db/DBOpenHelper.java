package sam.richwandell.com.myapplication.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import sam.richwandell.com.myapplication.MainActivity;
import static sam.richwandell.com.myapplication.RV.TAG;

public class DBOpenHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 8;

    private static final String DATABASE_NAME = "data.db";

    private static final String SCAN_TABLE_CREATE =
            "create table scan (s_id INTEGER PRIMARY KEY);";

    private static final String SCAN_RESULTS_TABLE_CREATE =
            "CREATE TABLE scan_results (" +
                    "s_id INTEGER, " +
                    "fp_id TEXT, " +
                    "ap_id TEXT, " +
                    "x INTEGER, " +
                    "y INTEGER, " +
                    "value REAL, " +
                    "orig_values TEXT, " +
                    "created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, " +
                    "PRIMARY KEY (s_id, fp_id, ap_id)" +
                    ");";

    private static final String SCAN_RESULTS_DELETE = "drop table scan_results;";
    private static final String SCAN_DELETE = "drop table scan;";
    private final MainActivity main;

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.main = (MainActivity)context;
    }

    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "DBOpenHelper.onCreate");
        db.execSQL(SCAN_TABLE_CREATE);
        db.execSQL(SCAN_RESULTS_TABLE_CREATE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "DBOpenHelper.onUpgrade");
        db.execSQL(SCAN_DELETE);
        db.execSQL(SCAN_RESULTS_DELETE);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "DBOpenHelper.onDowngrade");
        onUpgrade(db, oldVersion, newVersion);
    }

    public static void debugAll(MainActivity main){
        Log.d(TAG, "DBOpenHelper.debugAll");
        DBOpenHelper mDbHelper = new DBOpenHelper(main);
        SQLiteDatabase readableDatabase = mDbHelper.getReadableDatabase();
        try (Cursor cursor = readableDatabase.query("scan_results", new String[]{
                        "s_id", "fp_id", "ap_id", "x", "y", "value", "orig_values", "created"},
                null, null, null, null, null)) {
            cursor.moveToFirst();
            do {
                int s_id = cursor.getInt(0);
                String fp_id = cursor.getString(1);
                String ap_id = cursor.getString(2);
                int x = cursor.getInt(3);
                int y = cursor.getInt(4);
                double value = cursor.getDouble(5);
                String orig_values = cursor.getString(6);
                String time = cursor.getString(7);
                Log.d(TAG, Integer.toString(s_id) + ", " +
                        fp_id + ", "
                        + ap_id + ", "
                        + Integer.toString(x) + ", "
                        + Integer.toString(y) + ", "
                        + Double.toString(value) + ", "
                        + orig_values + ", "
                        + time
                );

                cursor.moveToNext();
            } while (!cursor.isLast());
        } catch (Exception e) {
            Log.d(TAG, "DBOpenHelper.debugAll: " + e.toString());
            mDbHelper.close();
        } finally {
            mDbHelper.close();
        }
    }
}
