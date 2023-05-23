package si.uni_lj.fe.tnuv.eggalert_v1.Calendar;

import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import si.uni_lj.fe.tnuv.eggalert_v1.R;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AlarmCal extends Fragment {

    //private CalendarDB dbHandler;
    private EditText eventText;
    private TextView dispText;
    String savedText;
    String selectedDate;
    //private SQLiteDatabase sqLiteDatabase;
    CalendarView calendarView;
    //gumb save
    Button buttonSave;

    Calendar clickedDate;
    //nov seznam eventov za ikone
    List<EventDay> events = new ArrayList<>();

    public AlarmCal(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false);

    }

    public void onViewCreated(View view, Bundle savedInstanceState){
        eventText = getView().findViewById(R.id.inputText);
        calendarView = getView().findViewById(R.id.matCalendar);
        dispText=getView().findViewById(R.id.textOutput);
        buttonSave=getView().findViewById(R.id.button_save);
        // Prikaz eventov z ikonami v CalendarView
        calendarView.setEvents(events);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //vstavimo datum in tekst v SQL, kličemo metodo btnSaveToSql
                btnSaveToSql(v);
                //addIcon();
                eventText.setText("");

            }
        });
        //poslušalec dogodka: objekt z metodo
        calendarView.setOnDayClickListener(new OnDayClickListener() {

            @Override
            //ob kliku na datum vrne eventDay->datum
            public void onDayClick(@NonNull EventDay eventDay) {
                Calendar date= eventDay.getCalendar();
                //pretvorba datuma
                int year = date.get(Calendar.YEAR);
                int month = date.get(Calendar.MONTH);
                int day = date.get(Calendar.DAY_OF_MONTH);
                // Create a Calendar object and set the clicked date
                Calendar clickedDate = Calendar.getInstance();
                clickedDate.set(year, month, day);
                // Create a SimpleDateFormat object with your desired date format
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                // Convert the clickedDate to a string
                selectedDate= sdf.format(clickedDate.getTime());
                //funkcija, ki prebere event iz SQL tabele EventCalendar pri izbranem datumu
                String savedText;
                savedText=readData();
                //Toast.makeText(getContext(), eventDay.getCalendar().getTime().toString(), Toast.LENGTH_SHORT).show();
                //add icon
                addIcon(date, savedText);
            }

        });

    }
    //metoda za vnos v SQL ob kliku na save button
    public void btnSaveToSql(View view){
        //text input
        String textInput=eventText.getText().toString().trim();
        //vstavimo datum in tekst v SQL, kličemo metodo insertCalendarData
        try {

            CalendarDB db = new CalendarDB(getContext());
            db.open();
            db.insertCalendarData(selectedDate, textInput);
            db.close();
            Toast.makeText(getContext(), "Successfully saved", Toast.LENGTH_SHORT).show();
        }
        catch (SQLException e){
        }
    }
    //prebere podatke
    public String readData(){
        try {
            CalendarDB db= new CalendarDB(getContext());
            //preverimo, če tabela obstaja
            db.open();
            //kličemo metodo ReadDataBase
            dispText.setText(db.ReadDatabase(selectedDate));
            savedText= db.ReadDatabase(selectedDate);
            db.close();
            return  savedText;
        }
        catch (SQLException e){
            Toast.makeText(getContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
            return  null;
        }
    }
    //doda ikono oz. sliko pod datum v koledarju
    public void addIcon(Calendar clickedDate, String savedText){
        //text input
        //String textInput=eventText.getText().toString().trim();
        String egg="egg";
        //preverimo, če je input text egg
        if (savedText != null && savedText.equals(egg)) {
            //EventDay selectedEvent = new EventDay(clickedDate, R.drawable.egg_alert_symbol_black);
            // preverimo, če je kliknjen datum že v events listu
            boolean dateExists = false;
            for (EventDay eventDay : events) {
                Calendar eventDate = eventDay.getCalendar();
                if (eventDate.get(Calendar.YEAR) == clickedDate.get(Calendar.YEAR) &&
                        eventDate.get(Calendar.MONTH) == clickedDate.get(Calendar.MONTH) &&
                        eventDate.get(Calendar.DAY_OF_MONTH) == clickedDate.get(Calendar.DAY_OF_MONTH)) {
                    dateExists = true;
                    break;
                }
            }
            //če ni v seznamu eventov, ga dodamo
            if (!dateExists) {
                //null check
                if (clickedDate != null) {
                    events.add(new EventDay(clickedDate, R.drawable.egg_alert_symbol, Color.parseColor("#228B22")));
                    // Set the selected event with the icon to the calendar view
                    calendarView.setEvents(events);
                    Toast.makeText(getContext(), "added egg", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(getContext(), "no event", Toast.LENGTH_SHORT).show();
            }
        }
    }

}