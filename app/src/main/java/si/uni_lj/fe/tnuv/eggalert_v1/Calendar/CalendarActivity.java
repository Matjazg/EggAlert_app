package si.uni_lj.fe.tnuv.eggalert_v1.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import si.uni_lj.fe.tnuv.eggalert_v1.MainActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.R;

public class CalendarActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnAlarms;
    Button btnQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        //zagon aktivnosti iz main activity
        Intent intent = getIntent();

        //gumba za prikaz fragmentov
        btnAlarms = findViewById(R.id.button_alarms);
        btnAlarms.setOnClickListener((View.OnClickListener) this);

        btnQuantity = findViewById(R.id.button_quantities);
        btnQuantity.setOnClickListener((View.OnClickListener) this);

        Toolbar toolbar = findViewById(R.id.calendar_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onClick(View v) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (v.getId()){
            case R.id.button_alarms:
                //prikaz fragmenta Alarms ob kliku na gumb btnAlarm
                //transakcija iz enega fragmenta v drugega (v bistvu prikaže drug fragment)
                fragmentManager.beginTransaction()
                        //replace(trenutni fragment, novi fragment): zamenjamo fragment z drugim,
                        .replace(R.id.fragmentContainerView2, AlarmCal.class, null)
                        .setReorderingAllowed(true)
                        .addToBackStack(null) // name can be null
                        .commit();
                break;

            case R.id.button_quantities:
                //prikaz fragmenta Alarms ob kliku na gumb btnAlarm
                //transakcija iz enega fragmenta v drugega (v bistvu prikaže drug fragment)
                fragmentManager.beginTransaction()
                        //replace(trenutni fragment, novi fragment): zamenjamo fragment z drugim,
                        .replace(R.id.fragmentContainerView2, Quantities.class, null)
                        .setReorderingAllowed(true)
                        .addToBackStack(null) // name can be null
                        .commit();

                break;

        }
    }

    @Override
    public void onBackPressed() {
        // Navigate back to the main activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    @Override
    /*
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }*/
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(myIntent);
        return true;
    }
}

