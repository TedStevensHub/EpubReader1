package com.tedgro.ted.epubreader;

import com.tedgro.ted.epubreader.HomeActivity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Ted on 4/15/2016.
 */
public class FileFragment extends AppCompatActivity {

    private ArrayList<String> imgHrefArray;
    private ArrayList<String> imgIdArray;
    private ArrayList<String> htmlHrefArray;
    private ArrayList<String> htmlIdArray;
    private ListView lv;

    //path to folder where all files are
    final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/eBooks/";

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.fragment_file);








        lv = (ListView) findViewById(R.id.fileListView);

        FileArrayList fal = new FileArrayList();
        final ArrayList<String> list = fal.getFiles();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        lv.setAdapter(arrayAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                final int p = position;

                AlertDialog.Builder confirmation = new AlertDialog.Builder(
                        FileFragment.this);


                confirmation.setTitle("Import");


                confirmation
                        .setMessage("Import book into your library?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                //is this book already imported?


                                //check if there is space on device for uncompressed files

                                //getFilesDir() needs to know which activity the fragment is apart of




                                //click event with position to get file name
                                String fileName = list.get(p);

                                //unzip and add database record

                                    unpackZip(PATH, fileName);



                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });


                AlertDialog alertDialog = confirmation.create();


                alertDialog.show();


            }
        });

    }




    public class myParser {

        String folderName;

        public myParser(String folderName) {
            this.folderName = folderName;
        }

        //does zipname pass to class or method of class
        public Book getMetaData() throws Exception {

            Book book = new Book();
            XmlPullParserFactory pullParserFactory;
            try {
                pullParserFactory = XmlPullParserFactory.newInstance();
                pullParserFactory.setNamespaceAware(false);

                XmlPullParser parser = pullParserFactory.newPullParser();

                String opfstr = getOpf(folderName);
                InputStream in_s = new FileInputStream(opfstr);
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                parser.setInput(in_s, null);

                //***do i have to set book=parseXML in order to receive the return object

                book=parseXML(parser);
                File opfpath = new File(opfstr);
                book.setPath(opfpath.getParent());

            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
        }
            return book;
        }
    }

    public void writeDirectory() {

    }






    //meta data parser. can add other data logic
    private Book parseXML(XmlPullParser parser) throws XmlPullParserException,IOException {

        int eventType = parser.getEventType();
        Book book = new Book();
        Log.d("printlogger", "#1 in parseXML()");
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name;
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    Log.d("printlogger", "#2.1 in parseXML()");
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    //&& not already written to
                    if (name.equals("title") && book.getTitle().equals("")) {
                        Log.d("printlogger", "#3 in parseXML()");
                        book.setTitle(parser.nextText());
                        Log.d("printlogger", "#4 in parseXML() " + book.getTitle());
                    } else if (name.equals("creator") && book.getAuthor().equals("")) {
                        book.setAuthor(parser.nextText());
                    } else if (name.equals("description") && book.getDescription().equals("")) {
                        book.setDescription(parser.nextText());
                    } else if (name.equals("date") && book.getPubDate().equals("")) {
                        book.setPubDate(parser.nextText());
                    } else if (name.equals("item")) {
                        String mt = parser.getAttributeValue(null, "media-type");
                        if(mt.equals("image/jpeg")||mt.equals("image/png")||mt.equals("image/gif")) {
                            imgIdArray.add(parser.getAttributeValue(null, "id"));
                            imgHrefArray.add(parser.getAttributeValue(null, "href"));
                        } else if (mt.equals("application/xhtml+xml")) {
                            htmlIdArray.add(parser.getAttributeValue(null, "id"));
                            htmlHrefArray.add(parser.getAttributeValue(null, "href"));
                        }
                }

            }
            eventType = parser.next();
        }

        return book;
    }









    public class FileArrayList {

        ArrayList<String> list = new ArrayList<String>();
        // gets the files in the directory
        File fileDirectory = new File(Environment.getExternalStorageDirectory()+"/eBooks/");
        // lists all the files into an array
        File[] dirFiles = fileDirectory.listFiles(new EpubFileFilter(new String[] {"epub"}));

        //read files from eBooks folder and make an array of string
        public ArrayList<String> getFiles() {
            for (File file : dirFiles) {
                String strFileName = file.getName();
                list.add(strFileName);
            }
            return list;
        }

    }






    //file filter taking in array of file extensions, can be used to scan directories
    public static class EpubFileFilter implements FileFilter {

        private String[] okFileExtensions;
        /*private final String[] okFileExtensions =
                new String[] {"epub"};*/


        public EpubFileFilter (String[] extensions) {
            okFileExtensions = extensions;
        }

        public boolean accept(File file)
        {
            for (String extension : okFileExtensions)
            {
                if (file.getName().toLowerCase().endsWith(extension) || file.isDirectory())
                {
                    return true;
                }
            }
            return false;
        }
    }





    //loop the unzip up to 2 times with a counter, with an if statement. once to import meta file and read xml, then delete meta, change path, import all
    //getnextentry()-getname() returns the entire path including the name of the file
    private boolean unpackZip(String path, String zipname)
    {
        InputStream is;
        InputStream is2;
        ZipInputStream zis;
        ZipInputStream zis2;
        try
        {
            String filename;
            String zipnamereal=zipname;
            zipname=zipname.substring(0, zipname.lastIndexOf('.'));
            is2 = new FileInputStream(path + zipnamereal);
            zis2 = new ZipInputStream(new BufferedInputStream(is2));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;
            long size = 0;


            File bookFolder = new File(getFilesDir().getAbsolutePath() + "/" + zipname);

            if (bookFolder.exists()==false) {
                while ((ze = zis2.getNextEntry()) != null){
                    size += ze.getSize();
                    zis2.closeEntry();
                }
                zis2.close();

                is = new FileInputStream(path + zipnamereal);
                zis = new ZipInputStream(new BufferedInputStream(is));
                if (size <= Environment.getExternalStorageDirectory().getUsableSpace()) {
                    //do not need the line below
                    bookFolder.mkdirs();
                    Log.d("printlogger", "Did we get here? #1");
                    while ((ze = zis.getNextEntry()) != null) {
                        filename = ze.getName();
                        Log.d("printlogger", "Did we get here? #2" + ze.getName());
                        Log.d("printlogger", "Did we get here? #2");
                        // Need to create directories if not exists, or
                        // it will generate an exception...
                        File fmd = new File(getFilesDir().getAbsolutePath() + "/" + zipname + "/" + filename);
                        fmd.getParentFile().mkdirs();
                        /*if (ze.isDirectory()) {
                            Log.d("printlogger", "Did we get here? #3");
                            File fmd = new File(getFilesDir().getAbsolutePath() + "/" + zipname + "/" + filename);
                            fmd.mkdirs();
                            continue;
                        }*/


                        Log.d("printlogger", "Did we get here? #4");
                        FileOutputStream fout = new FileOutputStream(getFilesDir().getAbsolutePath() + "/" + zipname + "/" + filename);

                        Log.d("printlogger", getFilesDir().getAbsolutePath() + "/" + zipname + "/" + filename);
                        while ((count = zis.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                        }

                        fout.close();
                        zis.closeEntry();
                    }

                    zis.close();


                    addBook(zipname);

                    //send variables to main activity in order to update listview
                    Intent i = new Intent(FileFragment.this, HomeActivity.class);
                    startActivity(i);

                } else {
                    //toast, "device is full"
                    Toast.makeText(getApplicationContext(), "Not enough space on device.", Toast.LENGTH_LONG).show();

                }
            } else {
                //toast, "already in collection"
                Toast.makeText(getApplicationContext(), "Already added.", Toast.LENGTH_LONG).show();

            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }



    public void addBook(String fileName) {


        try {
            myParser mp = new myParser(fileName);
            //unsure if fileName passes in object or method
            Book book = mp.getMetaData();
            Log.d("printlogger", "After getMetaData()");

            //add book to database
            SQLiteOpenHelper helper = new HomeActivity.dbHelper(this);
            SQLiteDatabase db = helper.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();

            //trim file extension
            values.put("folder_name", fileName);
            values.put("title", book.getTitle());
            values.put("author", book.getAuthor());
            values.put("description", book.getDescription());
            values.put("date", book.getPubDate());
            values.put("resources_path", book.getPath());
            // Insert the new record
            db.insert("book", null, values);
            values.clear();

            for(int i=0; i<imgIdArray.size(); i++) {
                values.put("folder_name", fileName);
                values.put("type", "img");
                values.put("path", imgHrefArray.get(i));
                values.put("r_id", imgIdArray.get(i));
            }
            for(int i=0; i<htmlIdArray.size(); i++) {
                values.put("folder_name", fileName);
                values.put("type", "html");
                values.put("path", htmlHrefArray.get(i));
                values.put("r_id", htmlIdArray.get(i));
            }
            db.insert("resources", null, values);
            db.close();

            values.clear();
            imgHrefArray.clear();
            imgIdArray.clear();
            htmlHrefArray.clear();
            htmlIdArray.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public String getOpf(String foldername) {

        String path = getFilesDir().getAbsolutePath() + "/" + foldername;
        ArrayList<String> filePathList;
        PagerActivity.fileFinder ff = new PagerActivity.fileFinder();
        filePathList = ff.getFiles(new String[] {"opf"}, path);
        for (int i = 0; i < filePathList.size(); i++) {
        }
        return filePathList.get(0);
    }






    //Book object class
    public class Book {
        int _id = 0;
        String _foldername = "";
        String _title = "";
        String _author = "";
        String _description = "";
        String _date = "";
        String _path = "";

        public Book(){

        }



        public int getId(){
            return this._id;
        }

        public String getFolderName(){
            return this._foldername;
        }

        public String getTitle(){
            return this._title;
        }

        public String getAuthor(){
            return this._author;
        }

        public String getDescription(){
            return this._description;
        }

        public String getPubDate(){
            return this._date;
        }

        public void setFolderName(String foldername){
            this._foldername = foldername;
        }

        public void setTitle(String title){
            this._title = title;
        }

        public void setAuthor(String author){
            this._author = author;
        }

        public void setDescription(String description){
            this._description = description;
        }

        public void setPubDate(String date){
            this._date = date;
        }

        public void setPath(String path){
            this._path = path;
        }

        public String getPath(){
            return this._path;
        }
    }







}
