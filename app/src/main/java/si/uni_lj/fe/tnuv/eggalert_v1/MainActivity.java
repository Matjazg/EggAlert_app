package si.uni_lj.fe.tnuv.eggalert_v1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.material.navigation.NavigationView;

import si.uni_lj.fe.tnuv.eggalert_v1.Calendar.CalendarActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.Calendar.CalendarCardClickListener;
import si.uni_lj.fe.tnuv.eggalert_v1.Hatcheries.HatcheriesActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.Hatcheries.HatcheriesCardClickListener;
import si.uni_lj.fe.tnuv.eggalert_v1.Notifications.NotificationsActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.Notifications.NotificationsCardClickListener;
import si.uni_lj.fe.tnuv.eggalert_v1.Statistics.StatisticsActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.Statistics.StatisticsCardClickListener;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    private CardView cvHatcheries, cvNotifications, cvCalendar,cvStatistics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Hooks
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar =  findViewById(R.id.toolbar);
        //find card views
        cvHatcheries = findViewById(R.id.cvHatcheries);
        cvNotifications = findViewById(R.id.cvNotifications);
        cvCalendar = findViewById(R.id.cvCalendar);
        cvStatistics = findViewById(R.id.cvStatistics);


        //Toolbar
        setSupportActionBar(toolbar);


        //Navigation drawer menu
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();



        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        //setup on click listeners for the card views
        cvHatcheries.setOnClickListener(new HatcheriesCardClickListener(this));
        cvNotifications.setOnClickListener(new NotificationsCardClickListener(this));
        cvStatistics.setOnClickListener(new StatisticsCardClickListener(this));
        cvCalendar.setOnClickListener(new CalendarCardClickListener(this));


    }

    @Override
    protected void onResume() {
        super.onResume();

        navigationView.setCheckedItem(R.id.nav_home);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }else
        {
            super.onBackPressed();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId())
        {
            case R.id.nav_home:
                break;
            case R.id.nav_hatcheries:
                intent = new Intent(MainActivity.this, HatcheriesActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_notifications:
                intent = new Intent(MainActivity.this, NotificationsActivity.class);
                startActivity(intent);
                break;
            case  R.id.nav_calendar:
                intent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_statistics:
                intent = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_settings:
                Toast.makeText(this, "Settings are not implemented yet", Toast.LENGTH_SHORT).show();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}