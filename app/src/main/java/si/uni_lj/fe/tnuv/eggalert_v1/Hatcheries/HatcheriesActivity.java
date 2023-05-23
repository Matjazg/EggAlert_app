package si.uni_lj.fe.tnuv.eggalert_v1.Hatcheries;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import si.uni_lj.fe.tnuv.eggalert_v1.DeviceList.DeviceListActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.MainActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.R;
import si.uni_lj.fe.tnuv.eggalert_v1.blegatt.BluetoothLeService;
import si.uni_lj.fe.tnuv.eggalert_v1.blegatt.bleSensorData;

public class HatcheriesActivity extends AppCompatActivity implements HatcheriesAdapter.ItemClicked, HatcheryStateListener,HatcheriesAdapter.OnReconnectClickListener, BluetoothLeService.ConnectionListener {

    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private BluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter = null;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<Hatchery> hatcheries = new ArrayList<Hatchery>();
    HatcheriesAdapter myAdapter;
    Hatchery dummyHatchery = new Hatchery("Dummy", false, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hatcheries);
        Toolbar toolbar = findViewById(R.id.hatcheries_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Check if there is a saved state and restore it
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String hatcheriesJson = prefs.getString("hatcheries", null);
        if (hatcheriesJson != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Hatchery>>() {
            }.getType();
            hatcheries = gson.fromJson(hatcheriesJson, type);
        }

        // Add the add_hatchery element to the list
        if (hatcheries.size() == 0) {
            hatcheries.add(dummyHatchery);
        }

        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Bind to the BluetoothLeService
        Intent gattServiceIntent = new Intent(HatcheriesActivity.this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        myAdapter = new HatcheriesAdapter(this,hatcheries,mBluetoothLeService);
        myAdapter.setOnReconnectClickListener(this);
        recyclerView = findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(myAdapter);

    }

    @Override
    public void onItemClicked(int index) {
        if (index == hatcheries.size() - 1) {
            //hatcheries.add(index, new Hatchery("Kurnik " + Integer.toString(index + 1), false, "20", "999", false));
            //myAdapter.notifyDataSetChanged();
            Intent intent = new Intent(HatcheriesActivity.this, DeviceListActivity.class);
            startActivityForResult(intent, 2);

        } else {
            Toast.makeText(this, "Surname: " + hatcheries.get(index).getName(), Toast.LENGTH_SHORT).show();
        }

    }


    public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(myIntent);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current state of the hatcheries list to SharedPreferences
        saveHatcheriesListToSharedPreferences();
    }
    private void saveHatcheriesListToSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("hatcheries", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String hatcheriesJson = gson.toJson(hatcheries);

        editor.putString("hatcheries", hatcheriesJson);
        editor.apply();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Get the updated hatcheries list from the data intent
            ArrayList<Hatchery> updatedHatcheries =  data.getParcelableArrayListExtra("hatcheries");

            // Update the current hatcheries list and notify the adapter
            hatcheries.clear();
            hatcheries.addAll(updatedHatcheries);
            myAdapter.notifyDataSetChanged();

            // Save the updated hatcheries list to SharedPreferences
            Gson gson = new Gson();
            String hatcheriesJson = gson.toJson(hatcheries);
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("hatcheries", hatcheriesJson);
            editor.apply();
        }

        if (requestCode == 2 && resultCode == RESULT_OK) {
            // Get the new hatchery from the result intent
            String hatcheryJson = data.getStringExtra("newHatchery");

            if (hatcheryJson != null) {
                Gson gson = new Gson();
                Hatchery newHatchery = gson.fromJson(hatcheryJson, Hatchery.class);
                // Add the new hatchery to the hatcheries list
                hatcheries.add(hatcheries.size()-1,newHatchery);
                // Notify the adapter of the data change
                myAdapter.notifyDataSetChanged();

                // Save the updated hatcheries list to SharedPreferences
                gson = new Gson();
                String hatcheriesJson = gson.toJson(hatcheries);
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("hatcheries", hatcheriesJson);
                editor.apply();
            }


        }
    }
    @Override
    public void onBackPressed() {
        // Save the hatcheries to SharedPreferences
        Gson gson = new Gson();
        String hatcheriesJson = gson.toJson(hatcheries);
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("hatcheries", hatcheriesJson);
        editor.apply();

        super.onBackPressed();
    }
    @Override
    public void onHatcheryStateChanged(bleSensorData sensorData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BluetoothDevice bleDevice = sensorData.getBleDevice();
                if (bleDevice != null) {
                    for (Hatchery hatchery : hatcheries) {
                        String hatcheryDeviceAdress = hatchery.getBleDeviceAddress();
                        if (hatcheryDeviceAdress != null && bleDevice.getAddress().equals(hatcheryDeviceAdress)) {
                            hatchery.setTemperature(sensorData.getTemperature());
                            hatchery.setPressure(sensorData.getPressure());
                            hatchery.setEggPresence(sensorData.getEggPresence());
                            break; // Found the hatchery, no need to continue the loop
                        }
                    }
                    myAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BluetoothLeService.addHatcheryStateListener(this);
        Log.d(TAG, "addHatcheryStateListener");


    }
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // Service is connected, obtain a reference to the service
            BluetoothLeService.LocalBinder binder = (BluetoothLeService.LocalBinder) iBinder;
            mBluetoothLeService = binder.getService();
            mBluetoothLeService.initialize();
            myAdapter.updateBluetoothLeService(mBluetoothLeService);
            mBluetoothLeService.setConnectionListener(HatcheriesActivity.this);
            // Perform any necessary operations on the service
            // For example, retrieve sensor data or perform other actions
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // Service is disconnected, clean up any references
            mBluetoothLeService = null;
        }
    };
    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the activity as a listener for hatchery state updates
        BluetoothLeService.removeHatcheryStateListener(this);
        Log.d(TAG, "removeHatcheryStateListener");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }
    @Override
    public void onReconnectClick(int position) {
        reconnectHatchery(position);
    }
    private void reconnectHatchery(int position) {
        Hatchery hatchery = hatcheries.get(position);
        String deviceAddress = hatchery.getBleDeviceAddress();

        // Perform the reconnection logic using the BluetoothLeService
        if (mBluetoothLeService != null && deviceAddress != null) {
            // Call the appropriate method in BluetoothLeService to reconnect
            mBluetoothLeService.connect(deviceAddress);

            // Update the hatchery's connection status
            hatchery.setConnectionStatus(true);

            // Notify the adapter of the data change
            myAdapter.notifyDataSetChanged();

        }
    }
    private BluetoothGatt getBluetoothGatt() {
        if (mBluetoothLeService != null) {
            return mBluetoothLeService.getBluetoothGatt();
        } else {
            return null;
        }
    }
    @Override
    public void onDeviceConnected(String deviceAddress) {
        // Handle device connected event
    }

    @Override
    public void onDeviceDisconnected(String deviceAddress) {
        // Update the hatchery connection status based on the disconnected device address
        for (Hatchery hatchery : hatcheries) {
            String hatcheryAddress = hatchery.getBleDeviceAddress();
            if (hatcheryAddress != null && hatcheryAddress.equals(deviceAddress)) {
                hatchery.setConnectionStatus(false);

                // Save the updated hatchery list to SharedPreferences
                saveHatcheriesListToSharedPreferences();
                myAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

}

/*
V activtiyju narediš broadcastReciever ki čaka nove podatke
Naredi neko runnable nit, gre vprašat service za nov podatek
Naredi si nek data object za podajat podatke in v service narediš metodo ki pošilja podatke
 */