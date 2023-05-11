package si.uni_lj.fe.tnuv.eggalert_v1.Statistics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

import si.uni_lj.fe.tnuv.eggalert_v1.MainActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.R;

public class PlotsActivity extends AppCompatActivity {

    LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plots);

        lineChart = findViewById(R.id.line_chart);

        //objekt line dataset
        LineDataSet lineDataSet1 = new LineDataSet(dataValues1(), "Data set 1");
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        //dodamo podatke iz arraya v dataset
        dataSets.add(lineDataSet1);
        //dodamo dataset v graf
        LineData data = new LineData(dataSets);
        lineChart.setData(data);
        lineChart.invalidate();

        Toolbar toolbar = findViewById(R.id.plots_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private ArrayList<Entry> dataValues1()
    {
        ArrayList<Entry> dataVals = new ArrayList<Entry>();
        dataVals.add(new Entry(0,20));
        dataVals.add(new Entry(1,16));
        dataVals.add(new Entry(2,25));
        dataVals.add(new Entry(3,18));
        dataVals.add(new Entry(4,17));

        return dataVals;

    }
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), StatisticsActivity.class);
        startActivity(myIntent);
        return true;
    }
}