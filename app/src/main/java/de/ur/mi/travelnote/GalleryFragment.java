package de.ur.mi.travelnote;

import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import de.ur.mi.travelnote.de.ur.mi.travelnote.sqlite.helper.DatabaseHelper;


public class GalleryFragment extends Fragment {

    String userID;
    String userName;
    long deleteID;
    long sendID;
    private boolean fragmentStatus;
    private DatabaseHelper mDatabaseHelper;
    private GalleryCursorAdapter adapter;
    private TextView mTextView;
    private ListView mListView;
    ArrayList<String> listData;


    public GalleryFragment () {

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }



    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentStatus = true;

        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        mDatabaseHelper = new DatabaseHelper((getContext()));
        getUserInfo();

        initUi(view);

        new DisplayGalleryAsyncTask().execute();

        return view;
    }

    private void initUi(View view) {
        mTextView = (TextView) view.findViewById(R.id.album_name);
        mListView = (ListView) view.findViewById(R.id.gallery_list_view);
    }

    public void getUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userID = user.getUid();
            userName = user.getDisplayName();
        }
    }

    private void refreshFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, new GalleryFragment()).commit();
    }

    public void onResume() {
        super.onResume();
        if (!fragmentStatus) {
            fragmentStatus = true;
        }
    }

    public void onPause() {
        super.onPause();
        fragmentStatus = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatabaseHelper.close();
    }



    private class DisplayGalleryAsyncTask extends AsyncTask<Void, Void, Void>{
        Cursor data;

        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            data = mDatabaseHelper.getImagesCurrentUser(userID);
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            listData = new ArrayList<>();
            if(data == null || data.getCount() < 1){
                if (getView() != null) {

                }
            } else {
                try {
                    while(data.moveToNext()) {
                        listData.add(data.getString(1));
                    }
                } catch (CursorIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            adapter = new GalleryCursorAdapter(getContext(), data);
            mListView.setAdapter(adapter);

        }
    }
}
