package com.tedgro.ted.epubreader;

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



        HomeActivity.dbHelper helper = new HomeActivity.dbHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();



        Cursor c=db.query("book", new String[] {"title", "author", "description", "date"}, null, null, null, null, null);
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

        String info_string = infolist.get(1)+"\n"+infolist.get(2)+"\n"+"Description: "+infolist.get(3)+"\n"+"Date: "+infolist.get(4);

        TextView infotext = (TextView)findViewById(R.id.info_textview);
        infotext.setText(info_string);

    }





}