package de.ur.mi.travelnote;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class StartActivity extends AppCompatActivity {

    /*
        Implemented onNavigationItemSelectedListener to handle bottom navigation bar actions
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
                    if (getSupportFragmentManager().findFragmentById(R.id.map_fragment) == null) {
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
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, new OverviewFragment()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}

