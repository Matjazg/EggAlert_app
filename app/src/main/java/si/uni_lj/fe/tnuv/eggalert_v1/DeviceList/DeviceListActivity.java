package si.uni_lj.fe.tnuv.eggalert_v1.DeviceList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import si.uni_lj.fe.tnuv.eggalert_v1.Hatcheries.HatcheriesActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.Hatcheries.Hatchery;
import si.uni_lj.fe.tnuv.eggalert_v1.MainActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.R;
import si.uni_lj.fe.tnuv.eggalert_v1.blegatt.BluetoothLeService;
import si.uni_lj.fe.tnuv.eggalert_v1.blegatt.bleSensorData;

public class DeviceListActivity extends AppCompatActivity {

    private LeDeviceListAdapter mLeDeviceListAdapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private BluetoothLeService mBluetoothLeService;
    private boolean mBound = false;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothLeScanner mBluetoothLeScanner = null;

    private ArrayList<BluetoothDevice> mLeDevices = new ArrayList<BluetoothDevice>();
    public static final int REQUEST_BT_PERMISSIONS = 0;
    public static final int REQUEST_BT_ENABLE = 1;

    private boolean mScanning = false;
    private Handler mHandler = null;

    private Button btnScan = null;
    private Button btnNewAc = null;
    //BLE scanner video
    private ListView lvDevices;
    ArrayList<String> devicesList = new ArrayList<String>();
    private ArrayAdapter<String> listAdapter;
    private Handler handler = new Handler();

    private static final int PERMISSION_REQUEST_CODE = 123;


    Dialog customDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        btnScan = (Button) findViewById(R.id.btnScan);
        //create a simple array adapter to display scanned devices
        /*
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devicesList);
        lvDevices.setAdapter(listAdapter);*/

        //Toolbar
        Toolbar toolbar = findViewById(R.id.new_device_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        this.mHandler = new Handler();

        checkBtPermissions();
        enableBt();
        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter(this, mLeDevices);
        lvDevices = findViewById(R.id.list_devices);
        lvDevices.setAdapter(mLeDeviceListAdapter);

        btnScan.setOnClickListener(v ->
        {
            onBtnScan(v);
        });

        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                customDialog = new Dialog(DeviceListActivity.this);
                customDialog.setContentView(R.layout.connect_device_dialog);
                // Access the views within the dialog layout
                TextView tvDeviceName = customDialog.findViewById(R.id.tvDeviceName);
                EditText etHatcheryName = customDialog.findViewById(R.id.etHatcheryName);
                Button btnCancel = customDialog.findViewById(R.id.btnCancel);
                Button btnPair = customDialog.findViewById(R.id.btnPair);

                // Set the device name to the TextView
                tvDeviceName.setText(devicesList.get(position));

                // Set a click listener for the cancel button
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Dismiss the dialog when the cancel button is clicked
                        customDialog.dismiss();
                    }
                });

                // Set a click listener for the pair button
                btnPair.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(etHatcheryName.getText().toString().isEmpty())
                        {
                            Toast.makeText(DeviceListActivity.this, "Please enter a name!", Toast.LENGTH_SHORT).show();
                        }else {
                            // Get the custom name entered by the user
                            String customName = etHatcheryName.getText().toString().trim();
                            if (mLeDevices.get(position) == null) return;
                            Log.i("ListView", String.valueOf(position));
                            if (ActivityCompat.checkSelfPermission(DeviceListActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                                ActivityCompat.requestPermissions(DeviceListActivity.this,
                                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                        REQUEST_BT_ENABLE);
                            }
                            mDeviceAddress = mLeDevices.get(position).getAddress();
                            mDeviceName = mLeDevices.get(position).getName();
                            Log.i("Clicked Item", mDeviceName);
                            Log.i("Clicked Item", mDeviceAddress);

                            Intent gattServiceIntent = new Intent(DeviceListActivity.this, BluetoothLeService.class);
                            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


                            if (mScanning) {
                                mScanning = false;
                                scanLeDevice(false);
                                btnScan.setText(R.string.btnScan);
                            }
                            // Perform the pairing with the selected device using the custom name
                            // Add the paired device to your hatchery list
                            // Inside the onItemClick listener of the device list
                            String deviceName = customName; // Replace with the actual code to get the device name
                            String newDeviceAddress = mLeDevices.get(position).getAddress();
                            Hatchery newHatchery;
                            Intent resultIntent = null;
                            if (newDeviceAddress != null) {
                                newHatchery = new Hatchery(deviceName, true, newDeviceAddress);
                                Gson gson = new Gson();
                                String hatcheryJson = gson.toJson(newHatchery);
                                // Create an intent to pass the new hatchery back to the HatcheriesActivity
                                resultIntent = new Intent();
                                resultIntent.putExtra("newHatchery", hatcheryJson);
                            } else {
                                // Handle the case when newDevice is null
                            }
                            // Set the result and finish the DeviceListActivity
                            setResult(RESULT_OK, resultIntent);
                            // Dismiss the dialog after completing the pairing process
                            customDialog.dismiss();
                            //finish();
                        }
                    }
                });
                // Set the background of the dialog window to be transparent
                Window dialogWindow = customDialog.getWindow();
                if (dialogWindow != null) {
                    dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
                }
                customDialog.show();
            }
        });

    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            if (!mBluetoothLeService.initialize()) {
                Log.e("BLUETOOTH", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ...

        // Unbind from the BluetoothLeService
        //unbindService(mServiceConnection);
    }
    public void onBtnScan(View v) {
        if (mScanning) {
            mScanning = false;
            scanLeDevice(false);
            btnScan.setText(R.string.btnScan);
        } else {
            mScanning = true;
            scanLeDevice(true);
            btnScan.setText(R.string.btnScanStop);
        }
    }

    public void checkBtPermissions() {
        this.requestPermissions(
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_PRIVILEGED
                },
                REQUEST_BT_PERMISSIONS);
    }

    public void enableBt() {
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_BT_ENABLE);
            }else
            {
                startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
            }
        }
    }

    public void scanLeDevice(final boolean enable) {
        //ScanSettings mScanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build();

        if (enable) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_BT_PERMISSIONS);
            } else {
                //mScanning = true;
                Log.i("Scanning", "start");
                //mBluetoothLeScanner.startScan(mLeScanCallback);

                // Stops scanning after a predefined scan period.
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScanning = false;
                        if (ActivityCompat.checkSelfPermission(DeviceListActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(DeviceListActivity.this,
                                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                                    REQUEST_BT_PERMISSIONS);
                        }
                        mBluetoothLeScanner.stopScan((ScanCallback) mLeScanCallback);
                        btnScan.setText("SCAN");
                    }
                }, 10000);

                mScanning = true;
                mBluetoothLeScanner.startScan((ScanCallback) mLeScanCallback);

            }

        } else {
            Log.i("Scanning", "stop");
            mScanning = false;
            mBluetoothLeScanner.stopScan((ScanCallback) mLeScanCallback);
        }
    }



    private ScanCallback mLeScanCallback =
            new ScanCallback() {

                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if (ActivityCompat.checkSelfPermission(DeviceListActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DeviceListActivity.this,
                                new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                REQUEST_BT_PERMISSIONS);
                    }
                    String device = result.getDevice().getName();
                    if (device != null) {

                        String itemToAdd = result.getDevice().getName() + "\n" + result.getDevice().getAddress();
                        if(!devicesList.contains(itemToAdd))
                        {
                            Log.i("BLE", device.toString());
                            devicesList.add(itemToAdd);
                        }
                        mLeDeviceListAdapter.addDevice(result.getDevice());
                        mLeDeviceListAdapter.notifyDataSetChanged();
                        /*
                        if(!mLeDevices.contains(result.getDevice())) {
                            mLeDevices.add(result.getDevice());
                            if(device.equals("EggAlert"))
                            {
                                mDeviceAddress = result.getDevice().getAddress();
                                Intent gattServiceIntent = new Intent(DeviceListActivity.this, BluetoothLeService.class);
                                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                            }
                        }
                        listAdapter.notifyDataSetChanged();*/

                    }


                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    Log.i("BLE", "error");
                }
            };
    public void requestPermission(String permission) {
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), HatcheriesActivity.class);
        startActivity(myIntent);
        return true;
    }

}