package si.uni_lj.fe.tnuv.eggalert_v1.SQL_handling;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class BLEdb extends SQLiteOpenHelper {
        public static final String KEY_ROWID = "id";
        public static final String KEY_DATE = "date";
        public static final String KEY_EGG = "eggnumber";
        public static final String KEY_TEMP = "temperature";
        public static final String KEY_HUM = "humidity";

        public static final String DATABASE_NAME = "BLEdata";
        public static final String DATABASE_TABLE = "BLEtable";
        public static final int DATABASE_VERSION = 1;


        //class BLEdb ustvari database
        public BLEdb(Context context) {

                super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
                //oznake stolpcev
                String sqlCode = "Create table BLEtable " + "(" +
                        KEY_ROWID + "INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_DATE + "TEXT NOT NULL, " +
                        KEY_EGG + "TEXT NOT NULL, " +
                        KEY_TEMP + "INTEGER, " +
                        KEY_HUM + "INTEGER);";
                db.execSQL(sqlCode);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                db.execSQL("DROP TABLE IF EXISTS BLEtable");
                onCreate(db);

        }

        //Vstavljanje v SQL tabelo: method insertBLEData
        public boolean insertBLEData(String date, String eggcount, int temperature, int humidity) {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put("date", date);
                contentValues.put("egg", eggcount);
                contentValues.put("temperature", temperature);
                contentValues.put("humidity", humidity);
                //
                db.insert(DATABASE_TABLE, null, contentValues);
                return true;
        }

        //prebere vse podatke iz SQL datatbase in vrne arraylist
        public ArrayList<String> getBLEData(){
                ArrayList<String> bleData = new ArrayList<String>();

                SQLiteDatabase db= this.getReadableDatabase();
                //določimo stolpce v tabeli
                String[] projection = {
                        "id",
                        "date",
                        "eggnumber",
                        "temperature",
                        "humidity"
                };
                //Cursor
                Cursor cursor =db.query(DATABASE_TABLE,projection,null,null,null,null,null);
                //dodajamo v array
                while(cursor.moveToNext()) {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                        bleData.add(Integer.toString(id));
                        String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                        bleData.add(date);
                        String eggnumber = cursor.getString(cursor.getColumnIndexOrThrow("eggumber"));
                        bleData.add(eggnumber);
                        int temp=cursor.getInt(cursor.getColumnIndexOrThrow("temperature"));
                        bleData.add(Integer.toString(temp));
                        int hum=cursor.getInt(cursor.getColumnIndexOrThrow("humidity"));
                        bleData.add(Integer.toString(hum));

                }
                cursor.close();
                db.close();

                return bleData;
        }
    }

