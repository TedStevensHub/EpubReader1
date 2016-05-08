package com.tedgro.ted.epubreader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ted on 4/15/2016.
 */
public class FragmentInfo extends AppCompatActivity {

    ArrayList<String> infolist = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_info);

        String whereint = "";
        Integer bookpos = 0;
        Bundle extras = getIntent().getExtras();
        whereint = extras.getString("bookpos");
        bookpos = Integer.parseInt(whereint);
        bookpos+=1;
        whereint=Integer.toString(bookpos);


        /*Cursor c=db.query("book", new String[] {"title", "author", "description", "date"}, "id=?", new String[] {"1"}, null, null, null);*/

        HomeActivity.dbHelper helper = new HomeActivity.dbHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();



        Cursor c=db.query("book", new String[] {"title", "author", "description", "date"}, "id=?", new String[] {whereint}, null, null, null);
        if (c != null) {
            int i = 0;
            c.moveToFirst();
            while (i<c.getCount()) {
                String title = c.getString(c.getColumnIndex("title"));
                String author = c.getString(c.getColumnIndex("author"));
                String description = c.getString(c.getColumnIndex("description"));
                String date = c.getString(c.getColumnIndex("date"));
                infolist.add(title);
                infolist.add(author);
                infolist.add(description);
                infolist.add(date);
                i++;
                c.moveToNext();
            }
        }
        db.close();

        String info_string = infolist.get(0)+"\n"+infolist.get(1)+"\n\n"+"Description: "+infolist.get(2)+"\n\n"+"Date: "+infolist.get(3);

        TextView infotext = (TextView)findViewById(R.id.info_textview);
        infotext.setText(info_string);
        infolist.clear();

    }




}