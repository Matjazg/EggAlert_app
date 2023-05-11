package si.uni_lj.fe.tnuv.eggalert_v1.Calendar;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.applandeo.materialcalendarview.CalendarView;

import com.google.android.material.datepicker.MaterialCalendar;

import si.uni_lj.fe.tnuv.eggalert_v1.R;

/**
 * A simple {@link Fragment} subclass.

 * create an instance of this fragment.
 */
public class Quantities extends Fragment {
    CalendarView calendarView;


    public Quantities() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_quantities, container, false);
        calendarView =(CalendarView)v.findViewById(R.id.calendarQuantities);

        return  v;


    }

}