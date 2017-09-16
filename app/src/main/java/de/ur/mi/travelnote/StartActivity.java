package de.ur.mi.travelnote;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {


    private final String TAG = "Test..";

    /*
        Implemented onNavigationItemSelectedListener to handle bottom navigation bar actions, using anonymous class
         fragmentManager replaces fragment classes inside framelayout "content"
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    transaction.replace(R.id.content, new OverviewFragment()).commit();
                    return true;
                case R.id.navigation_diary:
                    transaction.replace(R.id.content, new DiaryFragment()).commit();
                    return true;
                case R.id.navigation_gallery:
                    transaction.replace(R.id.content, new GalleryFragment()).commit();
                    return true;
                case R.id.navigation_map:
                    if(getSupportFragmentManager().findFragmentById(R.id.mapFragment) == null){
                        transaction.replace(R.id.content, new MapFragment()).commitAllowingStateLoss();
                    }

                    return true;
                case R.id.navigation_settings:
                    transaction.replace(R.id.content, new SettingsFragment()).commit();
                    return true;
            }
            return false;
        }

    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.start_toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);

        //Setup Bottom Navigation Bar
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //Display "Overview Fragment" when activity is started first time



        /*

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_CONTACTS,
                }, 11);
                return;
            }
        }


        ListView lv = new ListView(this);
        setContentView(lv);
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        String[] from = { ContactsContract.Contacts.DISPLAY_NAME };
        int[] to = { android.R.id.text1 };
        ListAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, from, to, 0);
        lv.setAdapter(adapter);
        AdapterView.OnItemLongClickListener listener = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemLongClick position: " + position + ", id: " + id);
                return true;
            }
        };
        lv.setOnItemLongClickListener(listener);

        */


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, new OverviewFragment()).commit();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }



}

