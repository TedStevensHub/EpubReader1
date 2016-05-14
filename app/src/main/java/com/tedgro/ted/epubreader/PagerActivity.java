package com.tedgro.ted.epubreader;


import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Tedjo on 5/4/2016.
 */
public class PagerActivity extends FragmentActivity {

    ArrayList<String> pathList = new ArrayList<>();
    ArrayList<String> idList = new ArrayList<>();
    ArrayList<String> typeList = new ArrayList<>();
    String resources_path = "";

    private TextView textview;

    private static final int NUM_PAGES = 5;

    private ViewPager myPager;
    private PagerAdapter myPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scrollview_layout);

        //gather all metadata for when we need it
        String whereint = "";
        Integer bookpos = 0;
        Bundle extras = getIntent().getExtras();
        whereint = extras.getString("bookpos");
        bookpos = Integer.parseInt(whereint);
        bookpos+=1;
        whereint=Integer.toString(bookpos);

        HomeActivity.dbHelper helper = new HomeActivity.dbHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor c=db.query("book", new String[] {"resources_path"}, "id=?", new String[] {whereint}, null, null, null);
        if (c != null) {
            c.moveToFirst();
            resources_path = c.getString(c.getColumnIndex("resources_path"));
        }

        c=db.query("resources", new String[] {"type", "path", "r_id"}, "id=?", new String[] {whereint}, null, null, null);
        if (c != null) {
            int i=0;
            c.moveToFirst();
            while (i<c.getCount()) {
                String type = c.getString(c.getColumnIndex("type"));
                String path = c.getString(c.getColumnIndex("path"));
                String r_id = c.getString(c.getColumnIndex("r_id"));
                typeList.add(type);
                pathList.add(path);
                idList.add(r_id);
                i++;
                c.moveToNext();
            }
        }


        db.close();
        //end gathering meta data






        setContentView(R.layout.viewpager_layout);

        // Instantiate a ViewPager and a PagerAdapter.
        myPager = (ViewPager) findViewById(R.id.pager);
        myPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        myPager.setAdapter(myPagerAdapter);

        try {
            Spanned myspan = strToSpanned();
            textview.setText(myspan);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /*private Spanned editSpannable() {

    }*/


        public Spanned strToSpanned() throws Exception {
        String htmlstring = concatHtmlString();
        Spanned myspan = Html.fromHtml(htmlstring);
        return myspan;
    }

    //replace imagegetter with my own class
/*    public Spanned strToSpanned(String string) throws Exception {
        String htmlstring = concatHtmlString();
        Spanned myspan = Html.fromHtml(htmlstring, new Html.ImageGetter(){
            @Override public Drawable getDrawable(String source) {
                Drawable drawFromPath;
                int path = x;
                drawFromPath = (Drawable) PagerActivity.this.getResources().getDrawable(path);
                //set bounds
                drawFromPath.setBounds(0, 0, textview.getWidth(), textview.getHeight());
                return drawFromPath;
            }
        }, null);
        return myspan;
    }*/

    public String concatHtmlString() throws Exception {

        String htmlstring="";
        for (int i=0; i<pathList.size(); i++) {
            if (typeList.get(i).equals("html")) {

                htmlstring += "\n"+getStringFromFile(pathList.get(i));
            }
        }
        return htmlstring;
    }





    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String htmlString = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return htmlString;
    }



    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new PageFragment();
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }



    //what happens if its static
    @SuppressLint("ValidFragment")
    public class PageFragment extends Fragment {



        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.scrollview_layout, container, false);

            textview = (TextView) rootView.findViewById(R.id.booktextview);

            return rootView;
        }
    }




    @Override
    public void onBackPressed() {
        if (myPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            myPager.setCurrentItem(myPager.getCurrentItem() - 1);
        }
    }



    //file finding class that returns String Array list of absolute path
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
