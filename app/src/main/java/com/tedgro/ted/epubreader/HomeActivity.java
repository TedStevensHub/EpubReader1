package com.tedgro.ted.epubreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;


//MainActivity (not actually home)

public class HomeActivity extends AppCompatActivity {

    //get book titles from database
    ArrayList<String> list = new ArrayList<String>();
    ArrayList<String> alist = new ArrayList<String>();
    ArrayList<String> flist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home);


        dbHelper helper = new dbHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();



        Cursor c=db.query("book", new String[] {"title", "author", "folder_name"}, null, null, null, null, null);
        if (c != null) {
            int i = 0;
            c.moveToFirst();
                while (i<c.getCount()) {
                    String folder = c.getString(c.getColumnIndex("folder_name"));
                    String title = c.getString(c.getColumnIndex("title"));
                    String author = c.getString(c.getColumnIndex("author"));
                    flist.add(folder);
                    list.add(title);
                    alist.add(author);
                    i++;
                    c.moveToNext();
                }
        }
        db.close();


        ListView lView = (ListView)findViewById(R.id.bookListView);

        //instantiate custom adapter
        MyCustomAdapter adapter = new MyCustomAdapter(list, alist, this);

        //handle listview and assign adapter

        lView.setAdapter(adapter);

        assert lView != null;
        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                Log.d("intentlog", "#1 Position: " + Integer.toString(position));
                Intent i = new Intent(getApplicationContext(), PagerActivity.class);
                String pos = Integer.toString(position);
                i.putExtra("bookpos", pos);
                startActivity(i);
                Log.d("intentlog", "2");
            }

        });



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);

        return true;
    }

    //open file choosing fragment
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            Intent i = new Intent(HomeActivity.this, FileFragment.class);
            startActivity(i);
        }
//        return super.onOptionsItemSelected(item);
        return true;
    }





    public static class dbHelper extends SQLiteOpenHelper {

        public static final String DB_NAME = "books.db";
        public static final int DB_VERSION = 1;

        public dbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS book(id INTEGER PRIMARY KEY AUTOINCREMENT, folder_name nvarchar(150), title nvarchar(200), author nvarchar(200), description nvarchar(400), date nvarchar(50), resources_path nvarchar(400));");
            db.execSQL("CREATE TABLE IF NOT EXISTS resources(id INTEGER PRIMARY KEY AUTOINCREMENT, folder_name nvarchar(150), type nvarchar(50), path nvarchar(200), r_id nvarchar(100));");
            db.execSQL("CREATE TABLE IF NOT EXISTS spinetable(id INTEGER PRIMARY KEY AUTOINCREMENT, folder_name nvarchar(150), idref nvarchar(100));");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS book");
            db.execSQL("DROP TABLE IF EXISTS resources");
            db.execSQL("DROP TABLE IF EXISTS spinetable");

            // create fresh books table
            this.onCreate(db);
        }
    }






    //Custom listview generation (info, delete, book)
    public class MyCustomAdapter extends BaseAdapter implements ListAdapter {
        private ArrayList<String> bookList =  new ArrayList<String>();
        private ArrayList<String> authorList =  new ArrayList<String>();
        private Context context;

        public MyCustomAdapter(ArrayList<String> bookList, ArrayList<String> authorList, Context context) {
            this.bookList = bookList;
            this.authorList = authorList;
            this.context = context;
        }

        @Override
        public int getCount() {
            return bookList.size();
        }

        @Override
        public Object getItem(int pos) {
            return bookList.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return 0;
            //return bookList.get(pos).getId();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            //get nested layout
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.custom_list_layout, parent, false);
            }

            //set textview string
            TextView listItemText = (TextView)view.findViewById(R.id.list_book_string);
            TextView authorListText = (TextView)view.findViewById(R.id.list_author_string);
            listItemText.setText(bookList.get(position));
            authorListText.setText(authorList.get(position));
            listItemText.bringToFront();
//            listItemText.setTag(position);



/*            listItemText.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d("intentlog", "#1 Position: " + Integer.toString(position));
                    Intent i = new Intent(getApplicationContext(), PagerActivity.class);
                    i.putExtra("bookpos", position);
                    startActivity(i);
                    Log.d("intentlog", "2");
                }
            });*/

            //make buttons
            Button info_button = (Button)view.findViewById(R.id.bookInfo_btn);
            Button delete_button = (Button)view.findViewById(R.id.bookDelete_btn);

            //delete button event
            delete_button.setOnClickListener(new View.OnClickListener(){

                //confirmation popup


                @Override
                public void onClick(View v) {

                    AlertDialog.Builder confirmation = new AlertDialog.Builder(
                            context);


                    confirmation.setTitle("Confirm");


                    confirmation
                            .setMessage("Delete book from library?")
                            .setCancelable(false)
                            .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {

                                    //remove row
                                    bookList.remove(position); //or some other task
                                    authorList.remove(position);
                                    notifyDataSetChanged();

                                    //delete book files

                                    //delete page bookmark in preferences

                                    //delete from database

                                }
                            })
                            .setNegativeButton("No",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                }
                            });


                    AlertDialog alertDialog = confirmation.create();


                    alertDialog.show();
                }
            });





            //info button event
            info_button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    //open info activity
                    Intent i = new Intent(getApplicationContext(), FragmentInfo.class);
                    String pos = Integer.toString(position);
                    i.putExtra("bookpos", pos);
                    startActivity(i);
                }
            });



            return view;
        }
    }



//need file scan class









}
