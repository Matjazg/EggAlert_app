package si.uni_lj.fe.tnuv.eggalert_v1.SQL_handling;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import si.uni_lj.fe.tnuv.eggalert_v1.Calendar.CalendarDB;

public class BLEdb2 {

    public static final String KEY_ROWID = "id";
    public static final String KEY_DATE = "date";
    public static final String KEY_EGG = "eggnumber";
    public static final String KEY_TEMP = "temperature";
    public static final String KEY_PRE = "pressure";
    public static final String DATABASE_NAME = "calendarData";
    public static final String DATABASE_TABLE = "calendarTable";
    public static final int DATABASE_VERSION = 1;

    private DBHelper myHelper;
    private final Context myContext;
    private SQLiteDatabase myDatabase;

    public BLEdb2(Context context) {
        myContext=context;
    }
    private class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sqlCode = "CREATE TABLE " + DATABASE_TABLE + " (" +
                    KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_DATE + " TEXT NOT NULL, " +
                    KEY_EGG + " TEXT NOT NULL, " +
                    KEY_TEMP + " TEXT NOT NULL, " +
                    KEY_PRE+ " TEXT NOT NULL);";
            db.execSQL(sqlCode);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //pred drop table dodati, da ohraniš stare podatke not!!! ker drop table zbriše staro tabelo
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);

        }
    }
    //odpre database
    public BLEdb2 open() throws SQLException {
        myHelper = new DBHelper(myContext);
        myDatabase = myHelper.getWritableDatabase();
        return this;
    }
    public void close(){
        myHelper.close();
    }

    public boolean insertData(String selectedDate, String egg, String temp, String Pre) {
        ContentValues contentValues = new ContentValues();
        //shranimo datum v tabelo SQL
        contentValues.put(KEY_DATE, selectedDate);
        //shranimo nov event v tabelo
        contentValues.put(KEY_EGG, egg);
        contentValues.put(KEY_TEMP, temp);
        contentValues.put(KEY_PRE, Pre);
        //
        myDatabase.insert(DATABASE_TABLE, null, contentValues);
        Log.d("Calendar", "Insert successfull, date=" + selectedDate + "egg" + egg);
        return true;
    }

    //funkcija, ki prebere podatke iz SQL tabele calendarTable pri izbranem datumu
    public String ReadDatabase(String selectedDate) {
        //ime stolpca
        String[] columns = {"event"};
        //datum, pri katerem želimo prebrati tekst
        String selection = CalendarDB.KEY_DATE + " =?";
        String[] selectionArgs = {selectedDate};
        //
        Cursor cursor = myDatabase.query(
                DATABASE_TABLE,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        //
        if (cursor.moveToFirst()) {
            // Get the value in the "name" column
            String eventText = cursor.getString(cursor.getColumnIndexOrThrow("event"));
            Log.d("Calendar", "Datum: " + selectedDate);
            Log.d("Calendar", "Tekst: " + eventText);
            cursor.close();
            return eventText;
        } else {
            Log.d("Calendar", "No results found for date" + selectedDate);
            cursor.close();
            return null;
        }
    }
    //namesto te metode, uporabljena readDataBase
    public String ReadData(String selectedDate) {
        String query = "Select Event from calendarTable where date=" + selectedDate;

        try {
            //nastavimo kurzor, od kod začne brati tabelo
            Cursor cursor = myDatabase.rawQuery(query, null);
            cursor.moveToFirst();
            //tu spremeni prikaz alarma, zdaj prikazuje kr v vnosnem polju
            String eventText = cursor.getString(0);
            return eventText;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
