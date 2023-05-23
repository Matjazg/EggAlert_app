package si.uni_lj.fe.tnuv.eggalert_v1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import si.uni_lj.fe.tnuv.eggalert_v1.Calendar.CalendarActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.Calendar.CalendarCardClickListener;
import si.uni_lj.fe.tnuv.eggalert_v1.DeviceList.DeviceListActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.Hatcheries.HatcheriesActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.Hatcheries.HatcheriesCardClickListener;
import si.uni_lj.fe.tnuv.eggalert_v1.Hatcheries.Hatchery;
import si.uni_lj.fe.tnuv.eggalert_v1.Notifications.NotificationsActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.Notifications.NotificationsCardClickListener;
import si.uni_lj.fe.tnuv.eggalert_v1.Statistics.StatisticsActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.Statistics.StatisticsCardClickListener;
import si.uni_lj.fe.tnuv.eggalert_v1.blegatt.BluetoothLeService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    private CardView cvHatcheries, cvNotifications, cvCalendar,cvStatistics;
    private ArrayList<Hatchery> hatcheries;
    private List<Hatchery> hatcheriesList = new ArrayList<>();
    private BluetoothAdapter mBluetoothAdapter = null;
    BluetoothLeService mBluetoothLeService;
    private static final long CONNECTION_TIMEOUT = 3000; // 10 seconds
    private Handler mHandler = new Handler();
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
        // Initialize the hatcheries list
        hatcheries = new ArrayList<>();
        //setup on click listeners for the card views
        cvHatcheries.setOnClickListener(new HatcheriesCardClickListener(this, hatcheries));
        cvNotifications.setOnClickListener(new NotificationsCardClickListener(this));
        cvStatistics.setOnClickListener(new StatisticsCardClickListener(this));
        cvCalendar.setOnClickListener(new CalendarCardClickListener(this));

        // Retrieve the hatcheries list from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String hatcheriesJson = sharedPreferences.getString("hatcheries", "");

        if (!hatcheriesJson.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Hatchery>>() {}.getType();
            hatcheriesList = gson.fromJson(hatcheriesJson, type);
        }
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


    }

    @Override
    protected void onResume() {
        super.onResume();

        navigationView.setCheckedItem(R.id.nav_home);
        loadHatcheriesFromSharedPreferences();
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
    private void saveHatcheriesToSharedPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String hatcheriesJson = gson.toJson(hatcheries);
        editor.putString("hatcheries", hatcheriesJson);
        editor.apply();
    }
    @Override
    protected void onPause() {
        super.onPause();
        saveHatcheriesToSharedPreferences();
    }
    private void loadHatcheriesFromSharedPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String hatcheriesJson = prefs.getString("hatcheries", null);
        if (hatcheriesJson != null) {
            Type type = new TypeToken<ArrayList<Hatchery>>(){}.getType();
            hatcheries = gson.fromJson(hatcheriesJson, type);
        } else {
            hatcheries = new ArrayList<>();
        }
    }
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // Service is connected, obtain a reference to the service
            BluetoothLeService.LocalBinder binder = (BluetoothLeService.LocalBinder) iBinder;
            mBluetoothLeService = binder.getService();
            mBluetoothLeService.initialize();
            for (Hatchery hatchery : hatcheriesList) {
                String hatcheryAddress = hatchery.getBleDeviceAddress();
                if (hatcheryAddress != null) {
                    // Connect to the Bluetooth device
                    mBluetoothLeService.connect(hatcheryAddress);

                    // Define a runnable to be executed after the timeout
                    Runnable connectionTimeoutRunnable = new Runnable() {
                        @Override
                        public void run() {
                            // Check if the device is connected
                            if (mBluetoothLeService.isDeviceConnected(hatcheryAddress)) {
                                hatchery.setConnectionStatus(true);
                                // Save the updated hatchery list to SharedPreferences
                                saveHatcheriesListToSharedPreferences();
                                // Perform the desired action
                                // ...
                            } else {
                                hatchery.setConnectionStatus(false);
                                // Save the updated hatchery list to SharedPreferences
                                saveHatcheriesListToSharedPreferences();
                                // Handle the case when the device does not connect within the timeout
                                // ...
                            }
                        }
                    };

                    // Schedule the runnable to be executed after the timeout
                    mHandler.postDelayed(connectionTimeoutRunnable, CONNECTION_TIMEOUT);
                } else {
                    hatchery.setConnectionStatus(false);
                    // Save the updated hatchery list to SharedPreferences
                    saveHatcheriesListToSharedPreferences();
                }
            }
            // Perform any necessary operations on the service
            // For example, retrieve sensor data or perform other actions
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // Service is disconnected, clean up any references
            mBluetoothLeService = null;
        }
    };
    private void saveHatcheriesListToSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("hatcheries", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String hatcheriesJson = gson.toJson(hatcheriesList);

        editor.putString("hatcheries", hatcheriesJson);
        editor.apply();
    }

}