package si.uni_lj.fe.tnuv.eggalert_v1.Hatcheries;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;

import si.uni_lj.fe.tnuv.eggalert_v1.R;
import si.uni_lj.fe.tnuv.eggalert_v1.blegatt.BluetoothLeService;

public class HatcheriesAdapter extends RecyclerView.Adapter<HatcheriesAdapter.ViewHolder>{
    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_LAST = 1;
    private ArrayList<Hatchery> hatcheries;
    private BluetoothLeService bleService;
    private OnToolbarItemClickListener toolbarItemClickListener;
    ItemClicked activity;

    private OnReconnectClickListener reconnectClickListener;
    public interface ItemClicked
    {
        void onItemClicked(int index);
    }
    public interface OnReconnectClickListener {
        void onReconnectClick(int position);
    }
    public void setOnReconnectClickListener(OnReconnectClickListener listener) {
        reconnectClickListener = listener;
    }
    public HatcheriesAdapter(Context context, ArrayList<Hatchery> list, BluetoothLeService bleService)
    {
        hatcheries = list;
        activity = (ItemClicked) context;
        this.bleService = bleService;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == hatcheries.size() - 1) {
            return VIEW_TYPE_LAST;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }
    public interface OnToolbarItemClickListener {
        void onToolbarItemClick(int position);


    }
    public void setOnToolbarItemClickListener(OnToolbarItemClickListener  listener) {
        toolbarItemClickListener = listener;
    }
    public class ViewHolder extends RecyclerView.ViewHolder
    {

        ImageView ivConnection, ivEgg, ivAddEgg;
        TextView tvName, tvEgg,tvAddEgg, tvTemperature, tvPressure;
        Toolbar toolbar;
        public ViewHolder(View itemView, int viewType)
        {
            super(itemView);
            if (viewType == VIEW_TYPE_NORMAL) {
                tvName = itemView.findViewById(R.id.tvName);
                tvEgg = itemView.findViewById(R.id.tvEgg);
                tvTemperature = itemView.findViewById(R.id.tvTemperature);
                tvPressure = itemView.findViewById(R.id.tvPressure);
                ivConnection = itemView.findViewById(R.id.ivConnection);
                ivEgg = itemView.findViewById(R.id.ivEgg);
                toolbar = itemView.findViewById(R.id.hatchery_options);

                toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            switch (item.getItemId()) {
                                case R.id.delete_hatchery:
                                    // Delete the hatchery from the list
                                    showDeleteConfirmationDialog(position, itemView);
                                    return true;
                                case R.id.reconnect_hatchery:
                                    // Reconnect the hatchery
                                    reconnectHatchery(position);
                                    return true;

                            }
                        }
                        return false;
                    }
                });

            } else {
                tvAddEgg = itemView.findViewById(R.id.tvAddEgg);
                ivAddEgg = itemView.findViewById(R.id.ivAddEgg);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.onItemClicked(hatcheries.indexOf( (Hatchery) itemView.getTag()));
                }
            });
        }
    }
    public void updateBluetoothLeService(BluetoothLeService service) {
        bleService = service;
    }
    private void reconnectHatchery(int position) {
        Hatchery hatchery = hatcheries.get(position);
        String deviceAddress = hatchery.getBleDeviceAddress();

        // Perform the reconnection logic using the BluetoothLeService
        if (bleService != null && deviceAddress != null) {
            // Call the appropriate method in BluetoothLeService to reconnect
            bleService.connect(deviceAddress);

            // Update the hatchery's connection status
            hatchery.setConnectionStatus(true);

            // Notify the adapter of the data change
            notifyDataSetChanged();
        }
    }
    @NonNull
    @Override
    public HatcheriesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == VIEW_TYPE_NORMAL) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.hatcheries_list, viewGroup, false);
            return new ViewHolder(view,VIEW_TYPE_NORMAL);
        } else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.hatcheries_add, viewGroup, false);
            return new ViewHolder(view,VIEW_TYPE_LAST);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HatcheriesAdapter.ViewHolder viewHolder, int position) {
        viewHolder.itemView.setTag(hatcheries.get(position));

        if (getItemViewType(position) == VIEW_TYPE_NORMAL) {
            viewHolder.tvName.setText(hatcheries.get(position).getName());
            viewHolder.tvTemperature.setText(hatcheries.get(position).getTemperature());
            viewHolder.tvPressure.setText(hatcheries.get(position).getPressure());

            if(hatcheries.get(position).getConnectionStatus() == true) {
                viewHolder.ivConnection.setImageResource(R.drawable.hatcheries_connected);
            } else {
                viewHolder.ivConnection.setImageResource(R.drawable.hatcheries_disconnected);
            }

            if(hatcheries.get(position).getEggPresence() == true) {
                viewHolder.ivEgg.setImageResource(R.drawable.happy_egg);
                viewHolder.tvEgg.setText("EGG");
            } else {
                viewHolder.ivEgg.setImageResource(R.drawable.sad_egg);
                viewHolder.tvEgg.setText("NO EGG");
            }

            // Set long click listener to delete the hatchery
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = viewHolder.getBindingAdapterPosition();
                    showDeleteConfirmationDialog(position,v);
                    return true;
                }
            });

        } else {
            viewHolder.tvAddEgg.setText("Add Hatchery");
            viewHolder.ivAddEgg.setImageResource(R.drawable.sad_egg);
        }
    }

    @Override
    public int getItemCount() {
        return hatcheries.size();
    }

    private void showDeleteConfirmationDialog(int position, View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setMessage("Are you sure you want to delete this hatchery?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteHatchery(position);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteHatchery(int position) {
        bleService.disconnect();
        hatcheries.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, hatcheries.size());
    }



}
