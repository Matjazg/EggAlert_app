package si.uni_lj.fe.tnuv.eggalert_v1.Hatcheries;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class HatcheriesCardClickListener implements View.OnClickListener {
    private AppCompatActivity activity;
    private ArrayList<Hatchery> hatcheries;
    public HatcheriesCardClickListener(AppCompatActivity activity,ArrayList<Hatchery> hatcheries)
    {
        this.activity = activity;
        this.hatcheries = hatcheries;
    }
    @Override
    public void onClick(View v) {
        // Handle click for Hatcheries card view
        /*Intent intent = new Intent(context, HatcheriesActivity.class);
        context.startActivity(intent);*/

        Intent intent = new Intent(activity, HatcheriesActivity.class);
        intent.putExtra("hatcheries", hatcheries);
        ((Activity) activity).startActivityForResult(intent, 1);
    }

}
