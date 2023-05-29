package si.uni_lj.fe.tnuv.eggalert_v1.Notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import si.uni_lj.fe.tnuv.eggalert_v1.Hatcheries.HatcheriesAdapter;
import si.uni_lj.fe.tnuv.eggalert_v1.Hatcheries.Hatchery;
import si.uni_lj.fe.tnuv.eggalert_v1.R;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private static List<Notifications> notificationList;
    static ItemClicked activity;

    public NotificationsAdapter(Context context, List<Notifications> notificationList) {
        activity = (ItemClicked) context;
        this.notificationList = notificationList;
    }

    public interface ItemClicked
    {
        void onItemClicked(int index);
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notifications_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Bind the notification data to the views in each item
        Notifications notification = notificationList.get(position);

        holder.tvNotificationMessage.setText(notification.getMessage());
        holder.tvNotificationTimeStamp.setText(formatTimestamp(notification.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNotificationMessage;
        TextView tvNotificationTimeStamp;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNotificationMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvNotificationTimeStamp = itemView.findViewById(R.id.tvNotificationTimeStamp);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.onItemClicked(notificationList.indexOf( (Hatchery) itemView.getTag()));
                }
            });
        }
    }

    private String formatTimestamp(String timestamp) {
        // Format the timestamp as per your requirement
        // For example, convert it to a readable date and time format
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}

