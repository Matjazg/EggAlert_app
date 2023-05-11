package si.uni_lj.fe.tnuv.eggalert_v1.SQL_handling;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DiarySQL extends SQLiteOpenHelper {
    public DiarySQL(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //onCreate: izvede samo, če file, ki je bil ustvarjen, še ne obstaja

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    //se izvede, če file že obstaja in je št. verzije nižja, kot tista v konstruktorju
    }
}
