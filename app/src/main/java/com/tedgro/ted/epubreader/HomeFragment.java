package com.tedgro.ted.epubreader;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ted on 4/15/2016.
 */
public class HomeFragment extends Fragment {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {

        View myFragmentView = inflater.inflate(R.layout.fragment_home, container, false);

        //begin list view
        //generate list
        ArrayList<String> list = new ArrayList<String>();
        list.add("The Farther Adventures of Robinson Crusoe ny Daniel Defoe");
        list.add("item2");

        //instantiate custom adapter
        MyCustomAdapter adapter = new MyCustomAdapter(list, myFragmentView.getContext());

        //handle listview and assign adapter
        ListView lView = (ListView)myFragmentView.findViewById(R.id.bookListView);
        lView.setAdapter(adapter);
        //end list view

        //return myFragmentView;
        return myFragmentView;

    }

    //open book start
    //class openBook()


    //open book end




    //Custom listview generation (info, delete, book)
    public class MyCustomAdapter extends BaseAdapter implements ListAdapter {
        private ArrayList<String> bookList =  new ArrayList<String>();
        private Context context;

        public MyCustomAdapter(ArrayList<String> bookList, Context context) {
            this.bookList = bookList;
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
            listItemText.setText(bookList.get(position));

            //make buttons
            Button info_button = (Button)view.findViewById(R.id.bookInfo_btn);
            Button delete_button = (Button)view.findViewById(R.id.bookDelete_btn);

            //delete button event
            delete_button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    //remove row
                    bookList.remove(position); //or some other task
                    notifyDataSetChanged();

                    //delete book files

                    //delete page bookmark in preferences

                }
            });

            //info button event
            info_button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    //open FragmentInfo and fragment_info

                    //fragment opener start
                        Fragment fr = new FragmentInfo();
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fm.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_place, fr);
                        fragmentTransaction.commit();
                    //fragment opener end

                }
            });

            return view;
        }
    }












}
