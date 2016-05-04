package com.tedgro.ted.epubreader;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Tedjo on 5/4/2016.
 */
public class PagerActivity extends AppCompatActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scrollview_layout);
    }

    public static class SeekBarFragment extends Fragment
    {

/*        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            View v = inflater.inflate(R.layout.hello_world,null);
            ListView mList =(ListView)v.findViewById(R.id.listview);
            ListAdapters adapter = new ListAdapters(getActivity().getApplicationContext());
            mList.setAdapter(adapter);
            return v;
        }*/
    }

}
