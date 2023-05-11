package si.uni_lj.fe.tnuv.eggalert_v1.Statistics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

import si.uni_lj.fe.tnuv.eggalert_v1.MainActivity;
import si.uni_lj.fe.tnuv.eggalert_v1.R;

public class StatisticsActivity extends AppCompatActivity implements View.OnClickListener{

    BarChart barChart;
    Button buttonPlots;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        //Prikaz histograma
        barChart = findViewById(R.id.histogram);

        //objekt podatkov
        BarDataSet barDataSet1 = new BarDataSet(dataValues1(),"DataSet1");
        barDataSet1.setColor(Color.parseColor("#596248"));

        //vsi podatki grafa
        BarData barData = new BarData();
        barData.addDataSet(barDataSet1);

        //
        barChart.setData(barData);
        barChart.invalidate();

        //prikaz grafov
        buttonPlots=findViewById(R.id.button4);
        buttonPlots.setOnClickListener(this);

        Toolbar toolbar = findViewById(R.id.statistics_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
    //ustvari array list s podatki
    private ArrayList<BarEntry> dataValues1(){
        ArrayList<BarEntry> dataVal= new ArrayList<>();
        dataVal.add(new BarEntry(0,3));
        dataVal.add(new BarEntry(1,4));
        dataVal.add(new BarEntry(3,6));

        return  dataVal;

    }

    //dodaten array list
    private ArrayList<BarEntry>dataValues2(){
        ArrayList<BarEntry> dataVal = new ArrayList<>();
        dataVal.add(new BarEntry(1.8f, 2));
        dataVal.add(new BarEntry(2, 8));
        dataVal.add(new BarEntry(3.6f, 3));

        return dataVal;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this,PlotsActivity.class);
        startActivity(intent);
    }
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(myIntent);
        return true;
    }
}