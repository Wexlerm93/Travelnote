package de.ur.mi.travelnote;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import de.ur.mi.travelnote.de.ur.mi.travelnote.sqlite.helper.DatabaseHelper;

public class NewDiaryEntryActivity extends AppCompatActivity {
    static final int DATE_ID = 0;
    private DatabaseHelper mDatabaseHelper;
    private String userID;
    private String userName;

    private EditText title, content, location, date;

    private int mYear, mMonth, mDay, sYear, sMonth, sDay;
    
    Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_diary_entry);
        Toolbar toolbar = (Toolbar) findViewById(R.id.basic_toolbar);
        setSupportActionBar(toolbar);

        mDatabaseHelper = new DatabaseHelper(this);
        getUserInfo();
        initTextFields();


        sMonth = calendar.get(Calendar.MONTH);
        sDay = calendar.get(Calendar.DAY_OF_MONTH);
        sYear = calendar.get(Calendar.YEAR);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        Button newEntry = (Button) findViewById(R.id.newDiaryEntryButton);
        newEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToDatabase();

            }
        });
        loadLatestState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_buttons_diary_entry_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_close_new_entry){
            clearLatestState();
            finish();
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        saveCurrentState();
        super.onBackPressed();
    }



    private void getDate(){
        date.setText(mDay+"."+(mMonth+1)+"."+mYear);
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener(){
                public void onDateSet(DatePicker view, int year,int monthOfYear,
                                      int dayOfMonth){
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    getDate();
                }
            };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id){
            case DATE_ID:
                return new DatePickerDialog(this, mDateSetListener,
                        sYear,sMonth,sDay);
        }
        return null;
    }

    private void getUserInfo(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userID = user.getUid();
            userName = user.getDisplayName();
        }
    }

    private void initTextFields(){
        title = (EditText) findViewById(R.id.newDiaryEntryTitle);
        content = (EditText) findViewById(R.id.newDiaryEntryContent);
        location = (EditText) findViewById(R.id.newDiaryEntryLocation);
        date = (EditText) findViewById(R.id.newDiaryEntryDate);
    }

    private void saveToDatabase(){
        String sTitle = title.getText().toString();
        String sContent = content.getText().toString();
        String sLocation = location.getText().toString();

        if(checkEmptyTextFields(sTitle, sContent, sLocation)){
            double lat = getLocation(sLocation)[0];
            double lng = getLocation(sLocation)[1];


            String sDate = mDay + "." + (mMonth+1) + "." + mYear;
            boolean insert = mDatabaseHelper.addDiaryEntry(sTitle,sContent,sLocation,lat,lng,sDate, userID, userName);
            if(insert){
                displayShortToast(R.string.entry_successful_toast);
                clearFields();
                title.requestFocus();
                clearLatestState();
            }else{
                displayShortToast(R.string.failed_saving_entry);
            }
        }else {
            displayShortToast(R.string.fields_missing_text);
        }

    }


    
    
    private boolean checkEmptyTextFields(String s1, String s2, String s3){
        if (s1.equals("") || s2.equals("") || s3.equals("")){
            return false;
        }else {
            return true;
        }
    }

    private double[] getLocation(String s){
        Geocoder geocoder = new Geocoder(this);
        double[] result = new double[2];

        if (!s.equals("")) {
            try {
                List<Address> list = geocoder.getFromLocationName(s, 1);
                if(list.size() > 0){
                    Address address = list.get(0);
                    result[0] = address.getLatitude();
                    result[1] = address.getLongitude();
                }else {
                    double BIASED_LAT = -66.666666;
                    result[0] = BIASED_LAT;
                    double BIASED_LNG = -145.678901;
                    result[1] = BIASED_LNG;
                }
            } catch (IOException e) {
                e.printStackTrace();
                displayShortToast(R.string.map_entry_failed);
            }

        } else {
            displayShortToast(R.string.location_missing);
        }
        return result;
    }

    private void displayShortToast(int id) {
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
    }

    private void clearFields(){
        title.setText("");
        content.setText("");
        location.setText("");
        date.setText("");
    }

    private void saveCurrentState(){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("title", title.getText().toString());
        editor.putString("content", content.getText().toString());
        editor.putString("location", location.getText().toString());
        //editor.putString("dateDay", date.getDayOfMonth().toString());
        editor.apply();
    }

    private void loadLatestState(){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String sTitle = sharedPreferences.getString("title", "");
        String sContent = sharedPreferences.getString("content", "");
        String sLocation = sharedPreferences.getString("location", "");
        String sDateDay = sharedPreferences.getString("dateDay", "");
        title.setText(sTitle);
        content.setText(sContent);
        location.setText(sLocation);
        //date.setText(sDate);
    }

    private void clearLatestState() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseHelper.close();
    }
}
