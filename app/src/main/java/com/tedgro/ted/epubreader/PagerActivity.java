package com.tedgro.ted.epubreader;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Tedjo on 5/4/2016.
 */
public class PagerActivity extends AppCompatActivity{

    ArrayList<String> filePathList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scrollview_layout);


        String folder;
        Bundle extras = getIntent().getExtras();
        folder = extras.getString("folder");

        fileFinder ff = new fileFinder();
        filePathList = ff.getFiles(new String[] {"html"}, getFilesDir().getAbsolutePath() + "/" + folder);


    }




    public static class fileFinder {

        // lists all the file paths into an array
        ArrayList<String> filePathList = new ArrayList<>();

        //search root and add file paths from every folder to an arraylist
        public ArrayList<String> getFiles(String[] type, String path) {

            //root directory to begin searching through files
            File fileDirectory = new File(path);
            Log.d("filefinder", path);
            File[] dirFiles = fileDirectory.listFiles(new FileFragment.EpubFileFilter(type));
            Log.d("filefinder", "#1");
            for (File file : dirFiles) {
                Log.d("filefinder", "#2");
                if ( file.isDirectory() ) {
                    Log.d("filefinder", "#3");
                    getFiles(type, file.getAbsolutePath());
                }
                else {
                    String strFilePath = file.getAbsolutePath();
                    filePathList.add(strFilePath);
                    Log.d("filefinder", "#4");
                }
            }
            return filePathList;
        }

    }




}
