package com.tedgro.ted.epubreader;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
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
public class FileFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {

        View myFragmentView = inflater.inflate(R.layout.fragment_file, container, false);

        //!!!!!!!!!!!!!!!get files needs to be the arraylist

        //ArrayList<String> list = getFiles();
        FileArrayList fal = new FileArrayList();
        final ArrayList<String> list = fal.getFiles();

        ListView fileList = (ListView)myFragmentView.findViewById(R.id.fileListView);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list);
        fileList.setAdapter(arrayAdapter);

        //arg0 and arg3 are not default, parent and id are
        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //getFilesDir() needs to know which activity the fragment is apart of
                Context context = getActivity();

                //path to folder where all files are
                final String PATH = Environment.getExternalStorageDirectory()+"/eBooks/";

                //click event with position to get file name
                String fileName = list.get(position);

                unpackZip(PATH, fileName, context);

            }
        });

        return myFragmentView;

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












    //getAbsolutePath()?

    //getnextentry()-getname() returns the entire path including the name of the file
    private boolean unpackZip(String path, String zipname, Context context)
    {
        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = new FileInputStream(path + zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            File bookFolder = new File(context.getFilesDir() + "/" + zipname);
            bookFolder.mkdirs();

            while ((ze = zis.getNextEntry()) != null)
            {
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an exception...
                if (ze.isDirectory()) {
                    File fmd = new File(context.getFilesDir() + "/" + zipname + "/" + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(context.getFilesDir() + "/" + zipname + "/" + filename);

                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }









}
