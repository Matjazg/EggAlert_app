package si.uni_lj.fe.tnuv.vaja_tecaj;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Alarms} factory method to
 * create an instance of this fragment.
 */
public class Alarms extends Fragment {


    //Prikaz koledarja
    private DiarySQL dbHandler;
    private EditText editText;
    private CalendarView calendarView;
    String selectedDate;
    private SQLiteDatabase sqLiteDatabase;

    //gumb save
    Button btnSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alarms, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState){
        //obvezno getView v fragmentu!!
        editText = getView().findViewById(R.id.editTextTextPersonName);
        calendarView = getView().findViewById(R.id.calendarView5);
        btnSave=getView().findViewById(R.id.buttonSave);
        //ob pritisku na gumb se izvede metoda InsertDatabase oz. shranimo tekst v SQL
        btnSave.setOnClickListener(this::InsertDatabase);
        //poslušalec dogodka: objekt z metodo
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                //se izvede, ko spremenimo datum-> vrne leto, mesec, dan
                selectedDate = Integer.toString(year) + Integer.toString(month) + Integer.toString(dayOfMonth);
                //kličemo metodo, ki prebere podatek iz SQL database
                ReadDatabase(view);

            }
        });
        //objekt, ki kreira tabelo v database
        try {
            //context preveri, če je OK!!
            dbHandler = new DiarySQL(getContext(), "CalendarDatabase", null, 1);
            sqLiteDatabase = dbHandler.getWritableDatabase();
            sqLiteDatabase.execSQL("Create table EventCalendar(Date TEXT, Event TEXT)");
        } catch (Exception e) {
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