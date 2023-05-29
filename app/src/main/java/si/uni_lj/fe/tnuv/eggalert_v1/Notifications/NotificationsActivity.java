package si.uni_lj.fe.tnuv.eggalert_v1.Notifications;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import si.uni_lj.fe.tnuv.eggalert_v1.DeviceList.DeviceListActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.Hatcheries.HatcheriesActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.MainActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.R;
import si.uni_lj.fe.tnuv.eggalert_v1.blegatt.BluetoothLeService;

public class NotificationsActivity extends AppCompatActivity implements NotificationsAdapter.ItemClicked {
    private BluetoothLeService mBluetoothLeService;
    private RecyclerView recyclerView;
    private NotificationsAdapter notificationsAdapter;
    private List<Notifications> notificationsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        Toolbar toolbar = findViewById(R.id.notifications_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent serviceIntent = new Intent(this, BluetoothLeService.class);
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        // Inside onCreate or any appropriate method
        recyclerView = findViewById(R.id.notification_list);
        notificationsAdapter = new NotificationsAdapter(this,notificationsList);
        recyclerView.setAdapter(notificationsAdapter);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(myIntent);
        return true;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // Service is connected, obtain a reference to the service
            BluetoothLeService.LocalBinder binder = (BluetoothLeService.LocalBinder) iBinder;
            mBluetoothLeService = binder.getService();
            notificationsList = mBluetoothLeService.getNotificationsList();
            notificationsAdapter.notifyDataSetChanged();
            // Now you have the reference to the BluetoothLeService and can use it as needed
            // You can store it in a member variable or pass it to other methods for further use
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // Service is disconnected, perform any necessary cleanup or handling
        }
    };

    @Override
    public void onItemClicked(int index) {
            Toast.makeText(this, notificationsList.get(index).getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        notificationsAdapter.notifyDataSetChanged();
    }
}