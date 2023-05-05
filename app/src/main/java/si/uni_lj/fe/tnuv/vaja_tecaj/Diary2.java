package si.uni_lj.fe.tnuv.vaja_tecaj;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;

import java.util.Date;

public class Diary2 extends AppCompatActivity {

    private DiarySQL dbHandler;
    private EditText editText;
    private CalendarView calendarView;
    String selectedDate;
    private SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary2);

        Intent intent = getIntent();

        editText=findViewById(R.id.editTextTextPersonName);
        calendarView=findViewById(R.id.calendarView5);
        //poslušalec dogodka: objekt z metodo
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
            //se izvede, ko spremenimo datum-> vrne leto, mesec, dan
                selectedDate=Integer.toString(year)+Integer.toString(month)+Integer.toString(dayOfMonth);
                //kličemo metodo, ki prebere podatek iz SQL database
                ReadDatabase(view);

            }
        });

        //objekt, ki kreira tabelo v database
        try {
            dbHandler= new DiarySQL(this,"CalendarDatabase",null,1);
            sqLiteDatabase=dbHandler.getWritableDatabase();
            sqLiteDatabase.execSQL("Create table EventCalendar(Date TEXT, Event TEXT)");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void InsertDatabase(View view){
        ContentValues contentValues =new ContentValues();
        //shranimo datum v tabelo SQL
        contentValues.put("Date",selectedDate);
        //shranimo nov event v tabelo
        contentValues.put("Event",editText.getText().toString());
        sqLiteDatabase.insert("EventCalendar", null,contentValues );

    }
    //funkcija, ki prebere podatke iz SQL database pri izbranem datumu
    public void ReadDatabase(View view){
        String query="Select Event from EventCalendar where Date="+ selectedDate;
        try {
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            cursor.moveToFirst();
            editText.setText(cursor.getString(0));
        }
        catch (Exception e){
            e.printStackTrace();
            editText.setText("");
        }
    }
}