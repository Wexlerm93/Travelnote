package de.ur.mi.travelnote;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import de.ur.mi.travelnote.de.ur.mi.travelnote.sqlite.helper.DatabaseHelper;

public class ShowLocationListActivity extends AppCompatActivity {

    String userName;
    String userID;
    TextView mTextView;
    ListView mListView;
    long deleteID;
    DatabaseHelper mDatabaseHelper;
    LocationCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_location_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.basic_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!= null){
            getSupportActionBar().setTitle("Deine Standorte");
        }
        getUserInfo();
        mDatabaseHelper = new DatabaseHelper(getApplicationContext());
        mTextView = (TextView) findViewById(R.id.location_empty_text);
        mListView = (ListView) findViewById(R.id.location_list_view);

        new DisplayLocationEntriesAsyncTask().execute();
        mTextView.setVisibility(View.GONE);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                showDeleteSingleEntryDialog(l);
                return true;
            }
        });
    }


    private void getUserInfo(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            userID = user.getUid();
            userName = user.getDisplayName();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_buttons_show_location_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_close_new_entry){
            finish();
            return true;

        }else{
            return super.onOptionsItemSelected(item);
        }
    }


    private void showDeleteSingleEntryDialog(long i) {
        this.deleteID = i;
        final int helper = (int) deleteID;
        //if there are db entries build alert dialog to avoid deletion by accident
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.delete_single_location_entry_warning_title);
        alertDialog.setMessage(R.string.delete_single_location_entry_warning_long);
        alertDialog.setIcon(R.drawable.ic_warning_black_24dp);

        //if user still clicks yes, then delete db entries
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mDatabaseHelper.clearLocationEntryCurrentUser(userID, helper);
                new DisplayLocationEntriesAsyncTask().execute();
                Toast.makeText(ShowLocationListActivity.this, "Eintrag wurde gelöscht", Toast.LENGTH_SHORT).show();
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseHelper.close();
    }

    private class DisplayLocationEntriesAsyncTask extends AsyncTask<Void,Void,Void> {

        ArrayList<String> listData;

        Cursor data;
        ProgressBar progressBar = new ProgressBar(getApplicationContext());

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            data = mDatabaseHelper.getMapCoordinates(userID);
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            listData = new ArrayList<>();
            if (data == null || data.getCount() < 1) {
                mTextView.setVisibility(View.VISIBLE);
                mTextView.setText("Keine Einträge vorhanden!");
            } else {
                try {
                    while (data.moveToNext()){
                        listData.add(data.getString(1));
                    }
                } catch (CursorIndexOutOfBoundsException e){
                    //...
                }
            }

            adapter = new LocationCursorAdapter(getApplicationContext(), data);
            mListView.setAdapter(adapter);

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            data.close();
        }
    }


}
