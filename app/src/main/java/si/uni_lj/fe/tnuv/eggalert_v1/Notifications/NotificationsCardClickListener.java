package si.uni_lj.fe.tnuv.eggalert_v1.Notifications;

import android.content.Context;
import android.content.Intent;
import android.view.View;

public class NotificationsCardClickListener implements View.OnClickListener {
    private Context context;
    public NotificationsCardClickListener(Context context)
    {
        this.context = context;
    }
    @Override
    public void onClick(View v) {
        // Handle click for Notifications card view
        Intent intent = new Intent(context, NotificationsActivity.class);
        context.startActivity(intent);
    }
}
