package de.ur.mi.travelnote;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView count;
    private Button delete;
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

        ///new DisplayGalleryAsyncTask().execute();

        return view;
    }

    private void initUi(View view) {
        mTextView = (TextView) view.findViewById(R.id.album_name);
        //shows count of how many pics are in the db...just used to check stuff
        count = (TextView) view.findViewById(R.id.count);
        count.setText(mDatabaseHelper.getCount());
    }

    /*
        Method to alert the user before deleting the gallery
     */

    private void showDeleteAllImagesDialog() {
        //if there are db entries build alert dialog to avoid deletion by accident
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Gallery löschen");
        alertDialog.setMessage("Willst du wirklich alle Bilder unwiederruflich läschen?");
        alertDialog.setIcon(R.drawable.ic_warning_black_24dp);

        //if user still clicks yes, then delete images of current user
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                boolean stmt = mDatabaseHelper.clearImagesCurrentUser(userID);
                if (stmt) {
                    Toast.makeText(getContext(), "Alle Bilder wurden gelöscht!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Bilder konnten nicht gelöscht werden.", Toast.LENGTH_SHORT).show();
                }
                refreshFragment();
            }
        });

        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do nothing
            }
        });
        alertDialog.show();
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

    /*
        Method to setup options menu of toolbar and inflate the toolbar's options menu
     */

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuInflater menuInflater = new MenuInflater(getContext());
        menuInflater.inflate(R.menu.action_button_gallery_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /*
        Method for selected toolbar's option item, what happens when a certain item is clicked
     */

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_delete_gallery:
                //check first if there are any images, show toast if not
                if (mDatabaseHelper.getCount() == "0") {
                    Toast.makeText(getContext(), "Keine Bilder vorhanden", Toast.LENGTH_SHORT).show();
                } else {
                    showDeleteAllImagesDialog();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

     /*
          Inner AsyncTask class to fetch diary entries from database and display entries using a custom adapter
     */

    /**private class DisplayGalleryAsyncTask extends AsyncTask<Void, Void, Void>{
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

        protected void onCancelled() {
            super.onCancelled();
            data.close();
        }
    }**/
}
