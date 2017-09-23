package de.ur.mi.travelnote;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;


public class GalleryFragment extends Fragment {

    GridView gridView;
    ArrayList<CustomImage> list;
    GalleryBaseAdapter adapter;
    ImageDBHelper mDatabase;
    ProgressBar progressBar;
    TextView  mTextView;
    Button newImage;

    String userID;

    private final int REQUEST_CODE = 999;
    private ImageView imageView;

    public GalleryFragment() {

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }



    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        gridView = (GridView) view.findViewById(R.id.grid_view_gallery);

        list = new ArrayList<>();
        adapter = new GalleryBaseAdapter(getActivity(), R.layout.gridview_gallery, list);
        gridView.setAdapter(adapter);
        newImage = (Button) view.findViewById(R.id.new_gallery_image);

        newImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),NewImageEntryActivity.class);
                startActivity(intent);
            }
        });

        mDatabase = new ImageDBHelper(getContext());

        initUIElements(view);
        initComponents();
        getUserInfo();

        //execute AsyncTask..
        new DisplayEntriesAsyncTask().execute();

        return view;
    }

    //init ui elements and set text view to invisible by default
    private void initUIElements(View view) {

        mTextView = (TextView) view.findViewById(R.id.gallery_empty_text);
        mTextView.setVisibility(View.GONE);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_loader);
    }

    //Init further necessary components
    private void initComponents() {

    }

    //get info of currently logged  in user
    private void getUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        }
    }


    // Method to start intent to get image, when permission is granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                Toast.makeText(getActivity(), R.string.cant_access_device, Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    // Get image from started intent
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /*
        Method to setup options menu of toolbar and inflate the toolbar's options menu
     */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuInflater menuInflater = new MenuInflater(getContext());
        menuInflater.inflate(R.menu.action_button_gallery_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /*
        Method for selected toolbar's option item, what happens when a certain item is clicked
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_gallery:
                //check first if there are any diary entries, show toast if not
                if (list.isEmpty()) {
                    Toast.makeText(getContext(), R.string.no_image, Toast.LENGTH_SHORT).show();
                } else {
                    showDeleteAllEntriesDialog();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void showDeleteAllEntriesDialog() {
        //if there are db entries build alert dialog to avoid deletion by accident
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.delete_db_gallery_entries_warning_title);
        alertDialog.setMessage(R.string.delete_db_gallery_entries_warning_long);
        alertDialog.setIcon(R.drawable.ic_warning_black_24dp);

        //if user still clicks yes, then delete diary entries of current user
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean stmt = mDatabase.clearImagesCurrentUser(userID);
                if (stmt) {
                    Toast.makeText(getContext(), R.string.all_pics_delete, Toast.LENGTH_SHORT).show();
                    refreshFragment();
                } else {
                    Toast.makeText(getContext(), R.string.couldnt_be_deleted, Toast.LENGTH_SHORT).show();
                }

            }
        });

        //if user cancels, do nothing
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing here.
            }
        });
        alertDialog.show();
    }

    //Method to reload the Fragment
    private void refreshFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, new DiaryFragment()).commit();
    }

    //Close database when fragment is destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatabase.close();
    }

    /*
              Inner AsyncTask class to fetch image entries from database and display entries using a custom adapter
         */
    private class DisplayEntriesAsyncTask extends AsyncTask<Void, Void, Void> {
        Cursor data;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        //Fetch database entries in background..
        @Override
        protected Void doInBackground(Void... voids) {
            data = mDatabase.getImages(userID);
            return null;
        }

        /*
            Method to do, when background task is finished
            fills ArrayList with data from database cursor , if there are any.
            update UI, when there are no database entries
            finally, set custom adapter to display entries in UI
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            list.clear();

            if (data != null && data.getCount() > 0) {
                if (data.moveToFirst()) {
                    do {
                        //int id = data.getInt(0);
                        String title = data.getString(1);
                        String location = data.getString(2);
                        byte[] image = data.getBlob(3);

                        list.add(new CustomImage(title, location, image));
                    } while (data.moveToNext());
                }
            } else {
                if (getView() != null) {
                    mTextView.setVisibility(View.VISIBLE);
                    mTextView.setText(R.string.no_diary_entries_text);
                }
            }

            adapter.notifyDataSetChanged();
        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
            data.close();
        }


    }

}
