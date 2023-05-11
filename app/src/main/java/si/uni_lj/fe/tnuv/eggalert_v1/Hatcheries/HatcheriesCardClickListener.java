package si.uni_lj.fe.tnuv.eggalert_v1.Hatcheries;

import android.content.Context;
import android.content.Intent;
import android.view.View;

public class HatcheriesCardClickListener implements View.OnClickListener {
    private Context context;
    public HatcheriesCardClickListener(Context context)
    {
        this.context = context;
    }
    @Override
    public void onClick(View v) {
        // Handle click for Hatcheries card view
        Intent intent = new Intent(context, HatcheriesActivity.class);
        context.startActivity(intent);
    }
}
