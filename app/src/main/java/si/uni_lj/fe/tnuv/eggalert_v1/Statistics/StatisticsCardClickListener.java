package si.uni_lj.fe.tnuv.eggalert_v1.Statistics;

import android.content.Context;
import android.content.Intent;
import android.view.View;

public class StatisticsCardClickListener implements View.OnClickListener {
    private Context context;
    public StatisticsCardClickListener(Context context)
    {
        this.context = context;
    }
    @Override
    public void onClick(View v) {
        // Handle click for Statistics card view
        Intent intent = new Intent(context, StatisticsActivity.class);
        context.startActivity(intent);
    }
}
