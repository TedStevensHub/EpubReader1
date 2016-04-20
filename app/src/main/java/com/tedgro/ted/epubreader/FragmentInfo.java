package com.tedgro.ted.epubreader;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Ted on 4/15/2016.
 */
public class FragmentInfo extends Fragment {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {
        return inflater.inflate(R.layout.fragment_info, container, false);
    }
}