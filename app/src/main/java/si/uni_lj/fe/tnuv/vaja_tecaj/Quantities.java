package si.uni_lj.fe.tnuv.vaja_tecaj;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

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
        View v= inflater.inflate(R.layout.fragment_quantities, container, false);
        calendarView = (CalendarView)v.findViewById(R.id.calendarView1);

        return v;
    }
}