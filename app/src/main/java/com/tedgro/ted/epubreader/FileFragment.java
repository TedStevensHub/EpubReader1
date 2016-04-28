package com.tedgro.ted.epubreader;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Ted on 4/15/2016.
 */
public class FileFragment extends AppCompatActivity {

    private ListView lv;

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
                                Context context = getApplicationContext();

                                //path to folder where all files are
                                final String PATH = Environment.getExternalStorageDirectory() + "/eBooks/";

                                //click event with position to get file name
                                String fileName = list.get(p);

                                //unzip and add database record
                                unpackZip(PATH, fileName, context);


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

        public Book getMetaData(String folderName2) throws Exception {
            Book book = new Book();
            XmlPullParserFactory pullParserFactory;
            try {
                pullParserFactory = XmlPullParserFactory.newInstance();

                XmlPullParser parser = pullParserFactory.newPullParser();

                InputStream in_s = getAssets().open(folderName2 + "/OEBPS/content.opf");
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                parser.setInput(in_s, null);

                parseXML(parser);

            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return book;
        }
    }








    //meta data parser. can add other data logic
    private Book parseXML(XmlPullParser parser) throws XmlPullParserException,IOException {

        int eventType = parser.getEventType();
        Book book = new Book();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = null;
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:

                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();

                    if (name == "dc:title" && book.getTitle() == "") {
                        book.setTitle(parser.nextText());
                    } else if (name == "dc:creator" && book.getAuthor() == "") {
                        book.setAuthor(parser.nextText());
                    } else if (name == "dc:description" && book.getDescription() == "") {
                        book.setDescription(parser.nextText());
                    } else if (name == "dc:date" && book.getPubDate() == "") {
                        book.setPubDate(parser.nextText());
                    }

/*                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("metadata") && book != null) {
                        products.add(currentProduct);
                    }*/
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
        File[] dirFiles = fileDirectory.listFiles(new EpubFileFilter());

        //read files from eBooks folder and make an array of string
        public ArrayList<String> getFiles() {
            for (File file : dirFiles) {
                String strFileName = file.getName();
                list.add(strFileName);
            }
            return list;
        }

    }






    //epub filter
    public class EpubFileFilter implements FileFilter {

        private final String[] okFileExtensions =
                new String[] {"epub"};

        public boolean accept(File file)
        {
            for (String extension : okFileExtensions)
            {
                if (file.getName().toLowerCase().endsWith(extension))
                {
                    return true;
                }
            }
            return false;
        }
    }






    //loop the unzip up to 2 times with a counter, with an if statement. once to import meta file and read xml, then delete meta, change path, import all
    //getnextentry()-getname() returns the entire path including the name of the file
    private boolean unpackZip(String path, String zipname, Context context)
    {

        try
        {
            InputStream is;
            ZipInputStream zis;
            String filename;
            is = new FileInputStream(path + zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;
            long size = 0;


            File bookFolder = new File(context.getFilesDir() + "/" + zipname);
            if (bookFolder.exists()==false) {

                while ((ze = zis.getNextEntry()) != null){
                    size += ze.getSize();
                }

                if (size <= Environment.getExternalStorageDirectory().getUsableSpace()) {

                    bookFolder.mkdirs();

                    while ((ze = zis.getNextEntry()) != null) {
                        filename = ze.getName();

                        // Need to create directories if not exists, or
                        // it will generate an exception...
                        if (ze.isDirectory()) {
                            File fmd = new File(context.getFilesDir() + "/" + zipname + "/" + filename);
                            fmd.mkdirs();
                            continue;
                        }

                        FileOutputStream fout = new FileOutputStream(context.getFilesDir() + "/" + zipname + "/" + filename);

                        while ((count = zis.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                        }

                        fout.close();
                        zis.closeEntry();
                    }

                    zis.close();

                    addBook(zipname);

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

        myParser mp = new myParser(fileName);
        try {
            Book book = mp.getMetaData(fileName);
            //add book to database



            SQLiteOpenHelper dbHelper = new HomeActivity.dbHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put("folder_name", fileName);
            values.put("title", book.getTitle());
            values.put("author", book.getAuthor());
            values.put("description", book.getDescription());
            values.put("date", book.getPubDate());

            // Insert the new record
            db.insert("book", null, values);




/*            SQLiteDatabase db;
            db=SQLiteDatabase.openDatabase("LibraryDB",  null, SQLiteDatabase.OPEN_READWRITE);
            db.execSQL("INSERT INTO book VALUES('"+fileName+"','"+book.getTitle()+"','"+
                    book.getAuthor()+"','"+book.getDescription()+"','"+book.getPubDate()+"');");*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    //Book object class
    public class Book {
        int _id = 0;
        String _foldername = "";
        String _title = "";
        String _author = "";
        String _description = "";
        String _date = "";

        public Book(){

        }

        public Book(int id, String foldername, String title, String author, String description, String date) {
            this._id=id;
            this._foldername=foldername;
            this._title=title;
            this._author=author;
            this._description=description;
            this._date=date;
        }

        public Book(String foldername, String title, String author, String description, String date) {
            this._foldername=foldername;
            this._title=title;
            this._author=author;
            this._description=description;
            this._date=date;
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
    }










}
