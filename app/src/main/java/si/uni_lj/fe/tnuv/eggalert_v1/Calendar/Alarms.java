package si.uni_lj.fe.tnuv.eggalert_v1.Calendar;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import si.uni_lj.fe.tnuv.eggalert_v1.R;
import si.uni_lj.fe.tnuv.eggalert_v1.SQL_handling.DiarySQL;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Alarms} factory method to
 * create an instance of this fragment.
 */
public class Alarms extends Fragment {


    //Prikaz koledarja
    private DiarySQL dbHandler;
    private EditText eventText;
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
        eventText = getView().findViewById(R.id.inputEvent);
        calendarView = getView().findViewById(R.id.calendarView5);
        btnSave=getView().findViewById(R.id.buttonSave);
        //ob pritisku na gumb se izvede metoda InsertDatabase oz. shranimo tekst v SQL
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InsertDatabase(v);
                Toast.makeText(getContext(), "Successfully saved", Toast.LENGTH_SHORT).show();
            }
        });
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
        //objekt, ki kreira tabelo v SQL database
        try {
            //context preveri, če je OK!!, name: ime tabele, version: št. verzije
            //pri fragmentu namesto this -> getContext()
            dbHandler = new DiarySQL(getContext(), "CalendarDatabase", null, 1);
            sqLiteDatabase = dbHandler.getWritableDatabase();
            sqLiteDatabase.execSQL("Create table EventCalendar(Date TEXT, Event TEXT)");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    //vstavljanje v tabelo
    public void InsertDatabase(View view){
        ContentValues contentValues =new ContentValues();
        //shranimo datum v tabelo SQL
        contentValues.put("Date",selectedDate);
        //shranimo nov event v tabelo
        contentValues.put("Event",eventText.getText().toString());
        sqLiteDatabase.insert("EventCalendar", null,contentValues );

    }
    //funkcija, ki prebere podatke iz SQL tabele EventCalendar pri izbranem datumu
    public void ReadDatabase(View view){
        String query="Select Event from EventCalendar where Date="+ selectedDate;
        try {
            //nastavimo kurzor, od kod začne brati tabelo
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            cursor.moveToFirst();
            //tu spremeni prikaz alarma, zdaj prikazuje kr v vnosnem polju
            eventText.setText(cursor.getString(0));
        }
        catch (Exception e){
            e.printStackTrace();
            eventText.setText("");
        }
    }
}