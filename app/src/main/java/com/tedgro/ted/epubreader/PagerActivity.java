package com.tedgro.ted.epubreader;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
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

    public static final int padding = 40;
    public ViewPager myPager;
    public TextView fragmentTextView;
    public static PagerAdapter myPagerAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("pagerview", "#0");
        setContentView(R.layout.scrollview_layout);
        Log.d("pagerview", "#0.1");

        String resources_path = "";
        ArrayList<String> pathList = new ArrayList<>();

        ArrayList<String> idList = new ArrayList<>();
        ArrayList<String> typeList = new ArrayList<>();
        ArrayList<String> spineList = new ArrayList<>();
        String folder = "";

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

        Cursor c=db.query("book", new String[] {"resources_path", "folder_name"}, "id=?", new String[] {whereint}, null, null, null);
        if (c != null) {
            c.moveToFirst();
            resources_path = c.getString(c.getColumnIndex("resources_path"));
            folder = c.getString(c.getColumnIndex("folder_name"));
            Log.d("pagerview", "folder name: "+folder);
        }

        c=db.query("resources", new String[] {"type", "path", "r_id"}, "folder_name=?", new String[] {folder}, null, null, null);
            while (c.moveToNext()) {
                String type = c.getString(c.getColumnIndex("type"));
                String path = c.getString(c.getColumnIndex("path"));
                String r_id = c.getString(c.getColumnIndex("r_id"));
                typeList.add(type);
                pathList.add(path);
                idList.add(r_id);
                Log.d("pagerview", "resources: "+r_id);
            }


        c=db.query("spinetable", new String[] {"idref"}, "folder_name=?", new String[] {folder}, null, null, null);
        while (c.moveToNext()) {
                String idref = c.getString(c.getColumnIndex("idref"));
                spineList.add(idref);
                Log.d("pagerview", "idref: "+idref);

            }


        db.close();
        //end gathering meta data



        Log.d("pagerview", "#1");
        setContentView(R.layout.viewpager_layout);
        Log.d("pagerview", "#2");
        // Instantiate a ViewPager and a PagerAdapter.
        myPager = (ViewPager) findViewById(R.id.pager);
        Log.d("pagerview", "#3");

        myPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        Log.d("pagerview", "#4");
        myPager.setAdapter(myPagerAdapter);
        Log.d("pagerview", "#5");


        initiateBook ib = new initiateBook();
        ArrayList<Spannable> pageArray = ib.initiateBook(spineList, idList, pathList, resources_path, myPager);
        Log.d("pagerview", "#16 Number of pages: " + pageArray.size());


        PageFragment.newInstance(1);
        Log.d("pagerview", "#17");

    }




    public static class initiateBook {
        //should go into class
        public static ArrayList<Spannable> pageArray = new ArrayList<>();

        public ArrayList<Spannable> initiateBook(ArrayList<String> spineList, ArrayList<String> idList, ArrayList<String> pathList, final String resources_path, ViewPager myPager) {
            try {
                Log.d("pagerview", "#6");
                strToSpanned str = new strToSpanned();
                Spanned myspan = str.strToSpanned(spineList, idList, pathList, resources_path, myPager);
                Log.d("pagerview", "#13");
                int boundsHeight = myPager.getHeight() - padding;
                Log.d("pagerview", "#13.1");
                int newPageLineBottom = 0;
                int lastPageLineBottom = 0;
                //myPager is wrong, that refrences a layout without a textview
                /*
                NEED A PREPPING FRAGMENT TO DUMP SPANNED INTO A TEXTVIEW
                */
                TextView prepTextView = (TextView) myPager.findViewById(R.id.prepTextView);
                Log.d("pagerview", "#13.2");
//                prepTextView.setVisibility(View.GONE);
                Log.d("pagerview", "#13.3");
                prepTextView.setText(myspan);
                Log.d("pagerview", "#13.4");
                int totalNumLines = prepTextView.getMaxLines();
                Log.d("pagerview", "#13.5");
                int startLine = 1;
                Spannable addpage = null;
                Log.d("pagerview", "#14");

                //does getlinebottom return heigh or total distance from top of textview
                for (int i = 1; i <= totalNumLines; i++) {
                    newPageLineBottom = prepTextView.getLayout().getLineBottom(i);

                    if (i == totalNumLines && newPageLineBottom - lastPageLineBottom <= boundsHeight) {
                        int start = prepTextView.getLayout().getLineStart(startLine);
                        int end = prepTextView.getLayout().getLineEnd(i);
                        TextUtils.copySpansFrom(myspan, start, end, null, addpage, 0);
                        pageArray.add(addpage);
                        addpage = null;
                    }

                    if (newPageLineBottom - lastPageLineBottom > boundsHeight) {
                        i = i - 1;
                        lastPageLineBottom = prepTextView.getLayout().getLineBottom(i);
                        int start = prepTextView.getLayout().getLineStart(startLine);
                        int end = prepTextView.getLayout().getLineEnd(i);
                        startLine = i + 1;
                        TextUtils.copySpansFrom(myspan, start, end, null, addpage, 0);
                        pageArray.add(addpage);
                        addpage = null;
                        i += 1;
                    }

                }
                prepTextView.setText("");
                Log.d("pagerview", "#15");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return pageArray;
        }
    }




    //return spannable of images and styled text
    public static class strToSpanned
        {

            public Spanned strToSpanned(ArrayList<String> spineList, ArrayList<String> idList, ArrayList<String> pathList, final String resources_path, final ViewPager myPager)throws Exception {

                Log.d("pagerview", "#7");
                concatHtmlString concat = new concatHtmlString();
                String htmlstring = concat.concatHtmlString(spineList, idList, pathList, resources_path);
                Log.d("pagerview", "#11");
                Spanned fullspan = Html.fromHtml(htmlstring, new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String source) {
                    String path = resources_path + "/" + source;
                    Drawable d = Drawable.createFromPath(path);


                    int finalw = 0;
                    int finalh = 0;
                    float widthpercentage = 0;
                    float heightpercentage = 0;
                    int widthbounds = myPager.getMeasuredWidth() - padding;
                    int heightbounds = myPager.getMeasuredHeight() - padding;
                    int truewidth = d.getIntrinsicWidth();
                    int trueheight = d.getIntrinsicHeight();

                    //scaling

                    widthpercentage = (truewidth - widthbounds) / truewidth;
                    heightpercentage = (trueheight - heightbounds) / trueheight;

                    if (widthpercentage == heightpercentage && widthpercentage >= 0) {
                        finalw = widthbounds;
                        finalh = heightbounds;
                    } else if (widthpercentage == heightpercentage && widthpercentage < 0) {
                        finalw = truewidth;
                        finalh = trueheight;
                    } else if (widthpercentage > heightpercentage) {
                        if (widthpercentage <= 0) {
                            //no scaling
                            finalw = truewidth;
                            finalh = trueheight;
                        } else {
                            //do scalling
                            finalw = (int) (truewidth - (truewidth * widthpercentage));
                            //use the dominant percentage that is width
                            finalh = (int) (trueheight - (trueheight * widthpercentage));
                        }
                    } else if (widthpercentage < heightpercentage) {
                        if (heightpercentage <= 0) {
                            finalw = truewidth;
                            finalh = trueheight;
                        } else {
                            finalw = (int) (truewidth - (truewidth * heightpercentage));
                            //use the dominant percentage that is height
                            finalh = (int) (trueheight - (trueheight * heightpercentage));
                        }
                    }

                    d.setBounds(0, 0, finalw, finalh);
                    return d;
                }
            }, null);
                Log.d("pagerview", "#12");

            return fullspan;


        }
    }

    public static class concatHtmlString {

        public String concatHtmlString(ArrayList<String> spineList, ArrayList<String> idList, ArrayList<String> pathList, final String resources_path) throws Exception {
            Log.d("pagerview", "#8");
            ArrayList<String> spinePathOrderedList;
            spinePathOrdered spo = new spinePathOrdered();
            spinePathOrderedList = spo.spinePathOrdered(spineList, idList, pathList, resources_path);
            Log.d("pagerview", "#9");
            String htmlstring = "";
            for (int i = 0; i < spinePathOrderedList.size(); i++) {
                Log.d("pagerview", spinePathOrderedList.get(i));
                htmlstring += getStringFromFile(spinePathOrderedList.get(i));
            }
            return htmlstring;
        }
    }

    public static class spinePathOrdered {

        public static ArrayList<String> spinePathOrdered(ArrayList<String> spineList, ArrayList<String> idList, ArrayList<String> pathList, final String resources_path) {

            ArrayList<String> spinePathOrderedList = new ArrayList<>();
            Log.d("pagerview", "#8.5");
            Log.d("pagerview",  "Error if nothing after this log" + Integer.toString(spineList.size()));
            for (int i = 0; i < spineList.size(); i++) {
                Log.d("pagerview", "spineorder outer loop");
                for (int ii = 0; ii < idList.size(); ii++) {
                    Log.d("pagerview", "spineorder inner loop");
                    if (spineList.get(i).equals(idList.get(ii))) {
                        Log.d("pagerview", "spineorder if inside loops" + resources_path + "/" + pathList.get(ii));
                        spinePathOrderedList.add(resources_path + "/" + pathList.get(ii));
                    }
                }
            }

            return spinePathOrderedList;
        }
    }





    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
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
        Log.d("pagerview", "#10");
        return htmlString;
    }


//was FragmentStatePagerAdapter, trying FragmentPagerAdapter
    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PageFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            //return number of pages
            return initiateBook.pageArray.size();
        }
    }


    //what happens if its static

    public static class PageFragment extends Fragment {


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            //Views were both ViewGroup, testing as View
            View rootView = inflater.inflate(R.layout.scrollview_layout, container, false);

            TextView fragmentTextView = (TextView) rootView.findViewById(R.id.booktextview);

            //add spanned object for the page they are going to
            fragmentTextView.setText(initiateBook.pageArray.get(getArguments().getInt("index")));




            return rootView;
        }



        public static PageFragment newInstance(int position) {
            PageFragment f = new PageFragment();
            Bundle args = new Bundle();
            args.putInt("index", position);
            f.setArguments(args);
            return f;
        }
    }



    @Override
    public void onBackPressed() {
        if (myPager.getCurrentItem() == 0) {
            // finishes the activity and calls backstack
            super.onBackPressed();
        } else {
            // Otherwise, select the previous page
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
