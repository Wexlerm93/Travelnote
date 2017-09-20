package de.ur.mi.travelnote;

import android.app.DatePickerDialog;
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
    private DatabaseHelper mDatabaseHelper;
    private String userID, userName, fullDate;
    private EditText title, content, location, date;
    private int mYear, mMonth, mDay, sYear, sMonth, sDay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_diary_entry);
        setupToolbar();
        initDefaultState();
        initNewEntry();
    }
    
    //method to setup toolbar
    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.basic_toolbar);
        setSupportActionBar(toolbar);
    }

    //method to init components for a new diary entry
    private void initNewEntry() {
        initTextFields();
        getDate();
        Button newEntry = (Button) findViewById(R.id.newDiaryEntryButton);
        newEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToDatabase();

            }
        });
    }


    //method to setup necessary functionality
    private void initDefaultState() {
        mDatabaseHelper = new DatabaseHelper(this);
        getUserInfo();
    }

    // Method to setup DatepickerDialog and get selected date
    private void getDate() {
        /*
                setup DatePicker Dialog, when user clicks on EditText field.
                in layout, the editText field is prevented to show keyboard, so only the dialog pops up
          */
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initCalendar();
                DatePickerDialog datePickerDialog = new DatePickerDialog(NewDiaryEntryActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        sDay = day;
                        sMonth = month + 1; //get human conform month
                        sYear = year;
                        fullDate = sDay + "." + sMonth + "." + sYear;
                        date.setText(fullDate);
                    }
                }, mYear, mMonth, mDay);
                //show the dialog
                datePickerDialog.show();

            }
        });
    }

    //init default calendar
    private void initCalendar() {
        final Calendar c = Calendar.getInstance();
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mMonth = c.get(Calendar.MONTH);
        mYear = c.get(Calendar.YEAR);
    }

    //method to get info of currently logged in user
    private void getUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userID = user.getUid();
            userName = user.getDisplayName();
        }
    }

    //method to initialize the editText fields
    private void initTextFields() {
        title = (EditText) findViewById(R.id.newDiaryEntryTitle);
        content = (EditText) findViewById(R.id.newDiaryEntryContent);
        location = (EditText) findViewById(R.id.newDiaryEntryLocation);
        date = (EditText) findViewById(R.id.newDiaryEntryDate);
    }

    //method to save inputs of editText fields into database
    private void saveToDatabase() {
        //get strings from input text fields
        String sTitle = title.getText().toString();
        String sContent = content.getText().toString();
        String sLocation = location.getText().toString();
        String sDate = date.getText().toString();

        //check if all fields are filled
        if (checkEmptyTextFields(sTitle, sContent, sLocation, sDate)) {
            //get location values
            double lat = getLocation(sLocation)[0];
            double lng = getLocation(sLocation)[1];

            //insert into database
            boolean insert = mDatabaseHelper.addDiaryEntry(sTitle, sContent, sLocation, lat, lng, sDate, userID, userName);

            //check if insertion was successful
            if (insert) {
                displayShortToast(R.string.entry_successful_toast);
                clearFields();
                title.requestFocus();
                clearLatestState();
            } else {
                displayShortToast(R.string.failed_saving_entry);
            }
        } else {
            displayShortToast(R.string.fields_missing_text);
        }
    }

    //checks if all editText fields aren't empty
    private boolean checkEmptyTextFields(String title, String content, String location, String date) {
        if (title.equals("") || content.equals("") || location.equals("") || date.equals("")) {
            return false;
        } else {
            return true;
        }
    }

    //method to get location coordinates from string using Geocoder
    private double[] getLocation(String s) {
        Geocoder geocoder = new Geocoder(this);
        double[] result = new double[2];

        //make sure input field is not empty
        if (!s.equals("")) {
            try {
                //get only first matchable address from geocoder
                List<Address> list = geocoder.getFromLocationName(s, 1);
                if (list.size() > 0) {
                    Address address = list.get(0);
                    result[0] = address.getLatitude();
                    result[1] = address.getLongitude();
                } else {
                    /* if input string is not a matchable address:
                        set "biased" coordinates to avoid app crash
                        biased coordinates are unlikely to reach by the user and will be caught
                        by this user can input nonsense and is still able to process
                     */
                    double BIASED_LAT = -66.666666;
                    result[0] = BIASED_LAT;
                    double BIASED_LNG = -145.678901;
                    result[1] = BIASED_LNG;
                }
            } catch (IOException e) {
                //catch if input can't be matched at all, so user can still process
                e.printStackTrace();
                displayShortToast(R.string.map_entry_failed);
            }
        } else {
            displayShortToast(R.string.location_missing);
        }
        return result;
    }

    //Method to show a short toast
    private void displayShortToast(int id) {
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
    }

    //Method to reset all editText fields to default state
    private void clearFields() {
        title.setText("");
        content.setText("");
        location.setText("");
        date.setText("");
    }

    /*
        SharedPreferences Methods
        Save the current values of the editText fields, when method is called (in onPause method)
     */

    private void saveCurrentState() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("title", title.getText().toString());
        editor.putString("content", content.getText().toString());
        editor.putString("location", location.getText().toString());
        editor.putString("date", date.getText().toString());
        editor.apply();
    }

    //method do load saved preferences in onResume (when user gets back to activity)
    private void loadLatestState() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String sTitle = sharedPreferences.getString("title", "");
        String sContent = sharedPreferences.getString("content", "");
        String sLocation = sharedPreferences.getString("location", "");
        String sDate = sharedPreferences.getString("date", "");
        title.setText(sTitle);
        content.setText(sContent);
        location.setText(sLocation);
        date.setText(sDate);
    }

    //method to clear SharedPreferences entries
    private void clearLatestState() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().apply();
    }


    //Setups toolbar's options menu and inflates the layout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_buttons_diary_entry_menu, menu);
        return true;
    }

    //Override method for what to do, if item from toolbar's options menu is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_close_new_entry) {
            clearFields();
            clearLatestState();
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    //load latest state from SharedPreferences in on Resume
    @Override
    protected void onResume() {
        super.onResume();
        loadLatestState();
    }

    //save current input values, when Activity is onPause
    @Override
    protected void onPause() {
        super.onPause();
        saveCurrentState();
    }

    //close db connection when Activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseHelper.close();
    }
}
