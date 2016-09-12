package sam.richwandell.com.myapplication.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import sam.richwandell.com.myapplication.RV;

public class DBOpenHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 5;

    public static final String DATABASE_NAME = "data.db";

    private static final String SCAN_TABLE_CREATE =
            "create table scan (s_id INTEGER PRIMARY KEY);";

    private static final String SCAN_RESULTS_TABLE_CREATE =
            "CREATE TABLE scan_results (" +
                    "s_id INTEGER, " +
                    "fp_id INTEGER, " +
                    "ap_id TEXT, " +
                    "coords TEXT, " +
                    "value REAL, " +
                    "orig_values TEXT, " +
                    "PRIMARY KEY (s_id, fp_id, ap_id)" +
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

    public static void debugAll(){
        DBOpenHelper mDbHelper = new DBOpenHelper(RV.mainActivity);
        SQLiteDatabase readableDatabase = mDbHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.query("scan_results", new String[]{
                        "s_id", "fp_id", "ap_id", "coords", "value", "orig_values"},
                null, null, null, null, null);
        try {
            cursor.moveToFirst();
            do {
                int s_id = cursor.getInt(0);
                int fp_id = cursor.getInt(1);
                String ap_id = cursor.getString(2);
                String coords = cursor.getString(3);
                double value = cursor.getDouble(4);
                String orig_values = cursor.getString(5);
                Log.d("rdebug", Integer.toString(s_id) + ", " +
                        Integer.toString(fp_id) + ", " + ap_id + ", " + coords + ", " +
                        Double.toString(value) + ", " + orig_values);

                cursor.moveToNext();
            } while (!cursor.isLast());
        }catch(Exception e){
            cursor.close();
            mDbHelper.close();
        }finally {
            cursor.close();
            mDbHelper.close();
        }
    }
}
