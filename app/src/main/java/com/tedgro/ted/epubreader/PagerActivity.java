package com.tedgro.ted.epubreader;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Tedjo on 5/4/2016.
 */

public class PagerActivity extends FragmentActivity {

    public static final int padding = 40;
    public static int boundsHeight = 0;
    public ViewPager myPager;
    public PagerAdapter myPagerAdapter;
    public TextView prepTextView;
    public RelativeLayout height;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("pagerview", "#0");
        setContentView(R.layout.viewpager_layout);
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



        //metrics
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        int width = size.x;
//        boundsHeight = height - padding;
        int boundsWidth = width - padding;


        //beging pagination

        spinePathOrdered spo = new spinePathOrdered();
        ArrayList<String> spinePathOrderedList = spo.spinePathOrdered(spineList, idList, pathList, resources_path);
        Log.d("new", "#6");


        concatHtmlString concat = new concatHtmlString();
        ArrayList<String> htmlStringArray = new ArrayList<>();
        try {
            htmlStringArray = concat.concatHtmlString(spinePathOrderedList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("new", "#7");

        strToSpanned str = new strToSpanned();
        try {
            strToSpanned.htmlSpannedArray.clear();
            str.strToSpanned(htmlStringArray, resources_path, boundsHeight, boundsWidth);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("new", "#8");

        prepTextView = (TextView) findViewById(R.id.prepTextView);

        Spanned myspan;
        Log.d("concater", "Size of spanned array "+Integer.toString(strToSpanned.htmlSpannedArray.size()));
        myspan = strToSpanned.htmlSpannedArray.get(0);
        for (int i=1;i<strToSpanned.htmlSpannedArray.size();i++) {
            Log.d("concater", "First "+Integer.toString(i));
            myspan = (Spanned) TextUtils.concat(myspan, strToSpanned.htmlSpannedArray.get(i));
            Log.d("concater", "Second "+Integer.toString(i));
        }


        myspanStatic mss = new myspanStatic();
        mss.makeMySpan(myspan);

//        prepTextView.setText(myspan);


        //end pagination onstart




/*        // Instantiate a ViewPager and a PagerAdapter.
       myPager = (ViewPager) findViewById(R.id.pager);

       //send pagearray to class to become static

       myPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());

       myPager.setAdapter(myPagerAdapter);*/


/*        PageFragment pf = new PageFragment();
       pf.newInstance(0);*/

        //end pagination for onCreate

        //a fragment may need to be displayed here, if it continues to crash
    }




    @Override
    protected void onStart() {
        super.onStart();

        height = (RelativeLayout) findViewById(R.id.height);

        boundsHeight = height.getHeight()-padding;

        initiateBook ib = new initiateBook();
        ArrayList<CharSequence> pageArray = ib.initiateBook();

        paStatic pastatic = new paStatic();
        pastatic.makePA(pageArray);

        // Instantiate a ViewPager and a PagerAdapter.
        myPager = (ViewPager) findViewById(R.id.pager);

        //send pagearray to class to become static

        myPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());

        myPager.setAdapter(myPagerAdapter);
//        PageFragment pf = new PageFragment();

        Log.d("pagerview", "Page Array Size: "+Integer.toString(paStatic.pa.size()));
       /*if(paStatic.pa==null) {
           pf.newInstance(-1);
       }else{
           pf.newInstance(0);
       }*/
        //pf.newInstance(0);
        Log.d("pagerview", "#17");



    }

/*    @Override
  public void onResume() {
      super.onResume();  // Always call the superclass method first


  }*/


    public static class paStatic {
        public static ArrayList<CharSequence> pa = new ArrayList<>();

        public void makePA(ArrayList<CharSequence> pageArray) {
            this.pa = pageArray;
        }
    }

    public static class myspanStatic {
        public static Spanned myspan;

        public void makeMySpan(Spanned myspan) {
            this.myspan = myspan;
        }
    }

    public class initiateBook {
        //should go into class
        public ArrayList<CharSequence> pageArray = new ArrayList<>();
        public TextView prepTextView;

        public ArrayList<CharSequence> initiateBook() {
            try {

                Log.d("pagerview", "#13");

                //does this trigger a redraw?
                //
                prepTextView = (TextView) findViewById(R.id.prepTextView);
                //
                prepTextView.setText(myspanStatic.myspan);

                int newPageLineBottom = 0;
                int lastPageLineBottom = 0;

                /////tree observation here!!!!!!!!
                int totalNumLines = prepTextView.getLineCount();
                Log.d("pagerview: ", "totalNumLines = "+Integer.toString(prepTextView.getLineCount()));

                //1
                int startLine = 1;
                CharSequence addpage = "";
                Log.d("pagerview", "#14");

                //i=1
                //does getlinebottom return heigh or total distance from top of textview
                for (int i = 1; i <= totalNumLines; i++) {
                    Log.d("pagination", "Line: "+Integer.toString(i));


                    /////tree observation here!!!!!!!!
                    newPageLineBottom = prepTextView.getLayout().getLineBottom(i);

                    if (i == totalNumLines && newPageLineBottom - lastPageLineBottom <= boundsHeight) {

                        /////tree observation here!!!!!!!!
                        int start = prepTextView.getLayout().getLineStart(startLine);
                        /////tree observation here!!!!!!!!
//                        int end = prepTextView.getLayout().getLineEnd(i);
                        int end = myspanStatic.myspan.length();

                        //myspan needs to be the concatenated span
//                        TextUtils.copySpansFrom(myspanStatic.myspan, start, end, null, addpage, 0);
                        addpage = myspanStatic.myspan.subSequence(start, end);
                        pageArray.add(addpage);
                    } else if (newPageLineBottom - lastPageLineBottom > boundsHeight) {
                        i = i - 1;
                        lastPageLineBottom = prepTextView.getLayout().getLineBottom(i);
                        int start = prepTextView.getLayout().getLineStart(startLine);
                        int end = prepTextView.getLayout().getLineEnd(i);
                        startLine = i + 1;
                        //4th parameter default null
//                        TextUtils.copySpansFrom(myspanStatic.myspan, start, end, null, addpage, 0);
                        addpage = myspanStatic.myspan.subSequence(start, end);
                        //app tries to add first page, and says it is null
                        pageArray.add(addpage);
                        i = i + 1;
                    }

                }
                //}
                prepTextView.setText("");
                Log.d("pagerview", "#15");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return pageArray;
        }

        public ArrayList<CharSequence> getPageArray(){
            return pageArray;
        }

    }




    //return spannable of images and styled text
    public static class strToSpanned {
        public static ArrayList<Spanned> htmlSpannedArray = new ArrayList<>();

        public void strToSpanned(ArrayList<String> htmlStringArray, final String resources_path, final int boundsHeight, final int boundsWidth) throws Exception {

            Log.d("pagerview", "#7");


            Log.d("spannedarray", Integer.toString(htmlStringArray.size()));
            Log.d("pagerview", "#11");


            for (int i = 0; i < htmlStringArray.size(); i++) {
                Log.d("pagerview", "#11.1");
                htmlSpannedArray.add(Html.fromHtml(htmlStringArray.get(i)/*, new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        String path = resources_path + "/" + source;
                        File f = new File(path);
                        Drawable d = Drawable.createFromPath(f.getAbsolutePath());


                        Log.d("pagerview", "#11.2");

                        int finalw = 0;
                        int finalh = 0;
                        float widthpercentage = 0;
                        float heightpercentage = 0;
                        int widthbounds = boundsWidth;
                        int heightbounds = boundsHeight;
                        Log.d("pagerview", "#11.3");
                        int truewidth = d.getIntrinsicWidth();
                        int trueheight = d.getIntrinsicHeight();
//                        Log.d("pagerview", "Is image working = " + Integer.toString(truewidth));

                        //scaling

                        if(truewidth==0||trueheight==0){

                        }else {

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
                                    //do scaling
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
                        }
                        d.setBounds(0, 0, finalw, finalh);
                        Log.d("pagerview", "#11.4");


                        return d;
                    }
                }, null*/));

                Log.d("pagerview", "#12");


            }
            //loop doesn't crash, but spanned array is not produced

            Log.d("pagerview", "Spanned array size = "+Integer.toString(htmlSpannedArray.size()));
//            return htmlSpannedArray;
        }
    }

    public class concatHtmlString {

        public ArrayList<String> concatHtmlString(ArrayList<String> spinePathOrderedList) throws Exception {
            Log.d("pagerview", "#8");

            Log.d("pagerview", "#9");
            ArrayList<String> htmlStringArray = new ArrayList<>();
            for (int i = 0; i < spinePathOrderedList.size(); i++) {
                Log.d("pagerview", spinePathOrderedList.get(i));

                //breaks here
                htmlStringArray.add(getStringFromFile(spinePathOrderedList.get(i)));
                Log.d("html string debug", "#9.1 Do I have string: "+htmlStringArray.get(i).toString());
            }
            return htmlStringArray;
        }
    }

    public class spinePathOrdered {

        public ArrayList<String> spinePathOrdered(ArrayList<String> spineList, ArrayList<String> idList, ArrayList<String> pathList, final String resources_path) {

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



    public String getStringFromFile(String filePath) throws Exception {

        String ret = "";


        try {
            FileInputStream inputStream = new FileInputStream (new File(filePath));

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }





    //was FragmentStatePagerAdapter, trying FragmentPagerAdapter
    private static class ScreenSlidePagerAdapter extends FragmentPagerAdapter {


        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
//            PageFragment pf = new PageFragment();

            return PageFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            //return number of pages
            //does this work, or is this called as the class is being created
            return 50;
        }
    }


    //what happens if its static

    public static class PageFragment extends Fragment {

        public TextView fragmentTextView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            //Views were both ViewGroup, testing as View
            View rootView = inflater.inflate(R.layout.scrollview_layout, container, false);

            fragmentTextView = (TextView) rootView.findViewById(R.id.booktextview);
            Bundle args = getArguments();
            int page = args.getInt("index");

            Log.d("pagerview", "Index #: "+Integer.toString(page));
            if(paStatic.pa.isEmpty()) {
                fragmentTextView.setText("");
            }else{
                fragmentTextView.setText(paStatic.pa.get(page));
            }

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
//        if (myPager.getCurrentItem() == 0) {
        // finishes the activity and calls backstack
        super.onBackPressed();
//        } else {
//            // Otherwise, select the previous page
//            myPager.setCurrentItem(myPager.getCurrentItem() - 1);
//        }
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


