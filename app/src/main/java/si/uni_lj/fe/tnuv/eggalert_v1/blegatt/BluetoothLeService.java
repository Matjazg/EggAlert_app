package si.uni_lj.fe.tnuv.eggalert_v1.blegatt;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;
import si.uni_lj.fe.tnuv.eggalert_v1.Calendar.CalendarDB;
import si.uni_lj.fe.tnuv.eggalert_v1.Hatcheries.HatcheryStateListener;
import si.uni_lj.fe.tnuv.eggalert_v1.Notifications.Notifications;
import si.uni_lj.fe.tnuv.eggalert_v1.R;
import si.uni_lj.fe.tnuv.eggalert_v1.SQL_handling.BLEdb2;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service implements EasyPermissions.PermissionCallbacks {
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private BluetoothDevice mBluetoothDevice;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private Boolean descriptorWriteComplete = false;
    List<BluetoothGattCharacteristic> characteristics = new ArrayList<BluetoothGattCharacteristic>();
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    public final static UUID UUID_TEMPERATURE =
            UUID.fromString(SampleGattAttributes.TEMPERATURE);
    public final static UUID UUID_HUMIDITY =
            UUID.fromString(SampleGattAttributes.PRESSURE);
    public final static UUID UUID_ML_PREDICT =
            UUID.fromString(SampleGattAttributes.ML_PREDICT);

    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final String CHANNEL_ID = "device_hatcheries_channel";
    private List<Notifications> notificationsList = new ArrayList<>();
    // Store the connected device addresses
    private Set<String> connectedDeviceAddresses = new HashSet<>();
    private bleSensorData sensorData = new bleSensorData();

    // Define a listener interface
    public interface ConnectionListener {
        void onDeviceConnected(String deviceAddress);

        void onDeviceDisconnected(String deviceAddress);
    }

    // Declare an instance variable for the listener
    private ConnectionListener connectionListener;

    // Set the listener
    public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }

    private static List<HatcheryStateListener> hatcheryStateListeners = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;

    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mBluetoothDevice = gatt.getDevice();
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);

                Log.i(TAG, "Connected to GATT server.");


                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;

                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                findGattServices(getSupportedGattServices());
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        private void findGattServices(List<BluetoothGattService> gattServices) {
            if (gattServices == null) return;
            String uuid = null;

            // Loops through available GATT Services.
            for (BluetoothGattService gattService : gattServices) {
                HashMap<String, String> currentServiceData = new HashMap<String, String>();
                uuid = gattService.getUuid().toString();

                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    if (UUID_TEMPERATURE.toString().equals(uuid) || UUID_HUMIDITY.toString().equals(uuid) || UUID_ML_PREDICT.toString().equals(uuid)) {
                        //notifiyCharacteristic(gattCharacteristic);
                        characteristics.add(gattCharacteristic);
                    }

                }

            }
            subscribeToCharacteristics(mBluetoothGatt);
        }

        private void notifiyCharacteristic(@NonNull BluetoothGattCharacteristic characteristic) {
            final int charaProp = characteristic.getProperties();

            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {

                setCharacteristicNotification(
                        characteristic, true);
                try {
                    Thread.sleep(300); // add 500ms delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        private void subscribeToCharacteristics(BluetoothGatt gatt) {
            if (characteristics.size() == 0) return;

            BluetoothGattCharacteristic characteristic = characteristics.get(0);
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            } else {
                gatt.setCharacteristicNotification(characteristic, true);
            }
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

            UUID uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid);
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                //descriptorWriteComplete = true;
            }
            characteristics.remove(0);
            subscribeToCharacteristics(gatt);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if (UUID_TEMPERATURE.equals(characteristic.getUuid())) {
            byte[] value = characteristic.getValue();
            int temperature = 0;
            if (value != null && value.length >= 2) {
                temperature = ((value[1] & 0xFF) << 8) | (value[0] & 0xFF);

            }
            Log.d(TAG, "Temperature: " + String.valueOf(temperature / 100.0));
            String temperature_str = String.valueOf(temperature / 100.0);
            intent.putExtra("TEMPERATURE", temperature_str);
        } else if (UUID_HUMIDITY.equals(characteristic.getUuid())) {
            byte[] value = characteristic.getValue();
            if (value != null && value.length == 4) {
                int humidity = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getInt();
                Log.d(TAG, "Humidity: " + String.valueOf(humidity / 10.0));
                String pressure_str = String.valueOf(humidity / 10.0);
                intent.putExtra("PRESSURE", pressure_str);

            }

        } else if (UUID_ML_PREDICT.equals(characteristic.getUuid())) {

            byte[] value = characteristic.getValue();
            int temperature = 0;
            if (value != null && value.length >= 2) {
                temperature = ((value[1] & 0xFF) << 8) | (value[0] & 0xFF);

            }
            String ML_str = String.valueOf(temperature);
            intent.putExtra("ML_PRED", ML_str);
            Log.d(TAG, "ML predict: " + String.valueOf(ML_str));
        }


        sendBroadcast(intent);
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Paired succsefulyl", Toast.LENGTH_SHORT).show();
                if (connectionListener != null) {
                    connectionListener.onDeviceConnected(mBluetoothDevice.getAddress());
                }
                //Ta funkcija prikaže stanje povezave, napiše v text view
                //updateConnectionState(R.string.connected);
                //invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

                //Ta funkcija prikaže stanje povezave, napiše v text view
                //updateConnectionState(R.string.disconnected);
                if (connectionListener != null) {
                    connectionListener.onDeviceDisconnected(mBluetoothDevice.getAddress());
                }
                //Spuca seznam servicov in napiše no data
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                String data;
                if (intent.hasExtra("PRESSURE")) {
                    data = intent.getStringExtra("PRESSURE");
                    sensorData.setPressure(data);

                } else if (intent.hasExtra("ML_PRED")) {
                    data = intent.getStringExtra("ML_PRED");
                    if (data.equals("1")) {
                        sensorData.setEggPresence(true);
                        // Create a Date object representing the current time
                        Date currentTime = new Date();

                        // Create a SimpleDateFormat object to format the timestamp
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        // Format the current time as a timestamp
                        String timestamp = dateFormat.format(currentTime);
                        // Create a Notification object
                        Notifications notification = new Notifications(mBluetoothDeviceAddress, "EggDetected", "Egg was detected in a hatchery",timestamp);
                        sendNotification(notification);

                    } else {
                        sensorData.setEggPresence(false);
                    }
                    saveToSQL(sensorData);
                } else if (intent.hasExtra("TEMPERATURE")) {
                    data = intent.getStringExtra("TEMPERATURE");
                    sensorData.setTemperature(data);
                    sensorData.setBleDevice(mBluetoothDevice);
                    notifyHatcheryStateChanged(sensorData);
                }


            }
        }
    };

    public BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        //close();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mGattUpdateReceiver);
        // Close the Bluetooth GATT connection when the service is destroyed
        // close();
    }

    public void closeConnection() {
        // Close the Bluetooth GATT connection explicitly
        close();

    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // Permission has not been granted, request it
                ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
            } else {
                // Permission has been granted, proceed with the Bluetooth operation
                if (mBluetoothGatt.connect()) {
                    mConnectionState = STATE_CONNECTING;
                    return true;
                } else {
                    return false;
                }
            }

        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Permission has not been granted, request it
            ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
        } else {
            // Permission has been granted, proceed with the Bluetooth operation
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        }
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void reconnect() {
        if (mBluetoothGatt != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // Permission has not been granted, request it
                ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
            } else {
                mBluetoothGatt.connect();
            }
        }
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Permission has not been granted, request it
            ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
        } else {
            // Permission has been granted, proceed with the Bluetooth operation
            mBluetoothGatt.disconnect();

        }

    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Permission has not been granted, request it
            ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
        } else {
            // Permission has been granted, proceed with the Bluetooth operation
            mBluetoothGatt.close();
        }

        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Permission has not been granted, request it
            ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
        } else {
            // Permission has been granted, proceed with the Bluetooth operation
            mBluetoothGatt.readCharacteristic(characteristic);
        }

    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
        } else {
            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }


        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid()) ||
                UUID_TEMPERATURE.equals(characteristic.getUuid()) ||
                UUID_HUMIDITY.equals(characteristic.getUuid()) ||
                UUID_ML_PREDICT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }

    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        // Permission granted, do something
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // Permission denied, show a message or do something else
    }

    private void handleNewSensorData(bleSensorData sensorData) {
        // Process the new sensor data

        // Update the hatchery state
        notifyHatcheryStateChanged(sensorData);
    }

    public bleSensorData getSensorData() {
        return sensorData;
    }

    public static void addHatcheryStateListener(HatcheryStateListener listener) {
        hatcheryStateListeners.add(listener);
    }

    public static void removeHatcheryStateListener(HatcheryStateListener listener) {
        hatcheryStateListeners.remove(listener);
    }

    private void notifyHatcheryStateChanged(bleSensorData sensorData) {
        for (HatcheryStateListener listener : hatcheryStateListeners) {
            listener.onHatcheryStateChanged(sensorData);
        }
    }

    // Method to check if a device is connected
    public boolean isDeviceConnected(String deviceAddress) {
        return connectedDeviceAddresses.contains(deviceAddress);
    }

    // Method to handle device connection
    private void handleDeviceConnection(String deviceAddress) {
        // Add the device address to the connected devices set
        connectedDeviceAddresses.add(deviceAddress);
        // Update the UI or perform any necessary operations
        // ...
    }

    // Method to handle device disconnection
    private void handleDeviceDisconnection(String deviceAddress) {
        // Remove the device address from the connected devices set
        connectedDeviceAddresses.remove(deviceAddress);
        // Update the UI or perform any necessary operations
        // ...
    }

    private int generateUniqueNotificationId() {
        return (int) System.currentTimeMillis();
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "EggAlert";
            String description = "Egg presence notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void sendNotification(Notifications notification) {
        // Assign a unique identifier to the notification
        int notificationId = generateUniqueNotificationId();
        // Store the notification in the notification history data source
        //notificationHistoryList.add(notification);
        // Create a notification channel (for Android 8.0 and above)
        createNotificationChannel();
        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.egg_alert_symbol)
                .setContentTitle("Egg Detected")
                .setContentText(notification.getMessage())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        }
        notificationManager.notify(notificationId, builder.build());

        notificationsList.add(notification);

    }
    private void saveToSQL(bleSensorData sensorData)
    {
        try {

            BLEdb2 db = new BLEdb2(this);
            db.open();
            db.insertData(sensorData.getTimeStamp(), Boolean.toString(sensorData.getEggPresence()), sensorData.getTemperature(), sensorData.getPressure());
            db.close();
            Toast.makeText(this, "Successfully saved", Toast.LENGTH_SHORT).show();
        }
        catch (SQLException e){
        }
    }
    public List<Notifications> getNotificationsList()
    {
        return notificationsList;
    }
}
