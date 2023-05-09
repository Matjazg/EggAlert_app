package si.uni_lj.fe.tnuv.eggalert_v1.SQL_handling;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class BLEdb extends SQLiteOpenHelper {
        public static final String KEY_ROWID = "_id";
        public static final String KEY_DATE = "_date";
        public static final String KEY_EGG = "_eggnumber";
        public static final String KEY_TEMP = "_temperature";
        public static final String KEY_HUM = "_humidity";

        public static final String DATABASE_NAME = "BLEdata";
        public static final String DATABASE_TABLE = "BLEtable";
        public static final int DATABASE_VERSION = 1;


        //class BLEdb ustvari database
        public BLEdb(Context context) {
            super(context, DATABASE_NAME,null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //oznake stolpcev
            String sqlCode = "Create table BLEtable " + "(" +
                    KEY_ROWID + "INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_DATE + "TEXT NOT NULL, " +
                    KEY_EGG + "TEXT NOT NULL, " +
                    KEY_TEMP +"INTEGER, " +
                    KEY_HUM + "INTEGER);";
            db.execSQL(sqlCode);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

