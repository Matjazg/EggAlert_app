package si.uni_lj.fe.tnuv.vaja_tecaj;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button buttonDiary;

    Button buttonStatistics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //gumb za zagon Diary aktivnosti
        buttonDiary=findViewById(R.id.button2);
        //se izvede ob kliku  na gumb
        buttonDiary.setOnClickListener(this);

        //prikaz stitistike
        buttonStatistics=findViewById(R.id.button3);
        buttonStatistics.setOnClickListener(this);


    }
    //na gumb se nanaša parameter oz. objekt v (od classa View)
    @Override
    public void onClick(View v)
     {
         switch (v.getId()){

             case R.id.button2:
                 Toast.makeText(this,"Klik na Diary", Toast.LENGTH_SHORT).show();
                 Intent intent = new Intent(this, Diary.class);
                 startActivity(intent);
                 break;
             case R.id.button3:
                 Toast.makeText(this,"Klik na Statistics", Toast.LENGTH_SHORT).show();
                 Intent intent1= new Intent(this, Statistics.class);
                 startActivity(intent1);
                 break;

         }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}