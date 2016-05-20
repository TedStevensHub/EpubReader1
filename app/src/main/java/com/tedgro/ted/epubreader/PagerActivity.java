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

    static final int padding = 40;
    public static ArrayList<String> pathList = new ArrayList<>();
    public ArrayList<String> absolutePathList = new ArrayList<>();
    public static ArrayList<String> idList = new ArrayList<>();
    public ArrayList<String> typeList = new ArrayList<>();
    public static ArrayList<String> spineList = new ArrayList<>();
    //add spine data

    public static ArrayList<Spanned> pageArray = new ArrayList<>();
    static String resources_path = "";
    String folder = "";

    public TextView fragmentTextView;


    public static ViewPager myPager;
    public PagerAdapter myPagerAdapter;

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

        Cursor c=db.query("book", new String[] {"resources_path", "folder_name"}, "id=?", new String[] {whereint}, null, null, null);
        if (c != null) {
            c.moveToFirst();
            resources_path = c.getString(c.getColumnIndex("resources_path"));
            folder = c.getString(c.getColumnIndex("folder_name"));
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
                absolutePathList.add(resources_path + "/" + path);
                idList.add(r_id);
                i++;
                c.moveToNext();
            }
        }

        c=db.query("spinetable", new String[] {"idref"}, "folder_name=?", new String[] {folder}, null, null, null);
        if (c != null) {
            int i=0;
            c.moveToFirst();
            while (i<c.getCount()) {
                String idref = c.getString(c.getColumnIndex("idref"));
                spineList.add(idref);
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
            int boundsHeight = myPager.getHeight() - padding;
            int newPageLineBottom = 0;
            int lastPageLineBottom = 0;
            TextView fragmentTextView = (TextView) myPager.findViewById(R.id.booktextview);
            fragmentTextView.setVisibility(View.GONE);
            fragmentTextView.setText(myspan);
            int totalNumLines = fragmentTextView.getMaxLines();
            int startLine = 1;
            Spannable addpage = null;




            //does getlinebottom return heigh or total distance from top of textview
            for (int i = 1;i<=totalNumLines ; i++) {
                newPageLineBottom = fragmentTextView.getLayout().getLineBottom(i);

                if (i==totalNumLines && newPageLineBottom - lastPageLineBottom <= boundsHeight) {
                    int start = fragmentTextView.getLayout().getLineStart(startLine);
                    int end = fragmentTextView.getLayout().getLineEnd(i);
                    TextUtils.copySpansFrom(myspan, start, end, null, addpage, 0);
                    pageArray.add(addpage);
                    addpage=null;
                }

                if (newPageLineBottom - lastPageLineBottom > boundsHeight) {
                    i=i-1;
                    lastPageLineBottom = fragmentTextView.getLayout().getLineBottom(i);
                    int start = fragmentTextView.getLayout().getLineStart(startLine);
                    int end = fragmentTextView.getLayout().getLineEnd(i);
                    startLine = i+1;
                    TextUtils.copySpansFrom(myspan, start, end, null, addpage, 0);
                    pageArray.add(addpage);
                    addpage=null;
                    i+=1;
                }

            }



        } catch (Exception e) {
            e.printStackTrace();
        }

        fragmentTextView.setVisibility(View.VISIBLE);
        PageFragment.newInstance(1);


    }





    //return spannable of images and styled text
    public static Spanned strToSpanned() throws Exception {
        String htmlstring = concatHtmlString();
        Spanned fullspan = Html.fromHtml(htmlstring, new Html.ImageGetter() {
            @Override public Drawable getDrawable(String source) {
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

                widthpercentage = (truewidth-widthbounds)/truewidth;
                heightpercentage = (trueheight-heightbounds)/trueheight;

                if (widthpercentage==heightpercentage && widthpercentage >= 0) {
                    finalw = widthbounds;
                    finalh = heightbounds;
                } else if (widthpercentage==heightpercentage && widthpercentage < 0) {
                    finalw = truewidth;
                    finalh = trueheight;
                } else if (widthpercentage>heightpercentage) {
                    if (widthpercentage<=0) {
                        //no scaling
                        finalw = truewidth;
                        finalh = trueheight;
                    } else {
                        //do scalling
                        finalw = (int) (truewidth - (truewidth * widthpercentage));
                        //use the dominant percentage that is width
                        finalh = (int) (trueheight - (trueheight * widthpercentage));
                    }
                } else if (widthpercentage<heightpercentage) {
                    if (heightpercentage<=0) {
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



        return fullspan;


    }



    public static ArrayList spinePathOrdered() {
        ArrayList<String> spinePathOrderedList = new ArrayList<>();

        for (int i=0; i<spineList.size();i++) {
            for (int ii=0; ii<idList.size(); ii++) {
                if (spineList.get(i).equals(idList.get(ii))) {
                    spinePathOrderedList.add(pathList.get(ii));
                }
            }
        }

        return spinePathOrderedList;
    }

    public static String concatHtmlString() throws Exception {
        ArrayList<String> spinePathOrderedList;
        spinePathOrderedList = spinePathOrdered();

        String htmlstring="";
        for (int i=0; i<spinePathOrderedList.size(); i++) {
            htmlstring += getStringFromFile(spinePathOrderedList.get(i))+"\n";
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



            return PageFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            //return number of pages

            return pageArray.size();
        }
    }


    //what happens if its static

    public static class PageFragment extends Fragment {


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.scrollview_layout, container, false);

            TextView fragmentTextView = (TextView) rootView.findViewById(R.id.booktextview);

            //add spanned object for the page they are going to
            fragmentTextView.setText(pageArray.get(getArguments().getInt("index")));




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
