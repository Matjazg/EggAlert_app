package si.uni_lj.fe.tnuv.eggalert_v1.Calendar;

import android.content.Context;
import android.content.Intent;
import android.view.View;

public class CalendarCardClickListener implements View.OnClickListener {
    private Context context;
    public CalendarCardClickListener(Context context)
    {
        this.context = context;
    }
    @Override
    public void onClick(View v) {
        // Handle click for Calendar card view
        Intent intent = new Intent(context, CalendarActivity.class);
        context.startActivity(intent);
    }
}
