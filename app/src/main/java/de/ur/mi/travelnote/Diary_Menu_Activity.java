package de.ur.mi.travelnote;

import android.content.Intent;
import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class Diary_Menu_Activity extends AppCompatActivity {

    private ArrayList<DiaryEntry> entries;
    private DiaryAdapter diary_adapter;
    private DiaryDbDatabase diaryDB;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        setContentView(R.layout.diary_menu);
        Toolbar diaryToolbar = (Toolbar) findViewById(R.id.diary_toolbar);
        setSupportActionBar(diaryToolbar);
        initEntryList();
        initDatabase();
        if (extras != null) {
            String content = extras.getString("content");
            String date = extras.getString("date");
            addNewEntry(content, date);
        }
        setupUI();
        refreshArrayList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_buttons_diary_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_diary:
                deleteDiaryEntries();
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /*
        Method defines what happens if user clicks back button in diary activity.
        This method provides, that a user does not get back to a previous filled diary entry form.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(Diary_Menu_Activity.this, MenuOverview.class));
        finish();
    }

    private void addNewEntry(String content, String date) {
        Date entryDate = getDateFromString(date);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(entryDate);

            DiaryEntry newEntry = new DiaryEntry(content, cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.MONTH), cal.get(Calendar.YEAR));
            diaryDB.insertMyObject(newEntry);
            refreshArrayList();
        }
    }

    private Date getDateFromString(String date) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN);
            try {
                return df.parse(date);
            } catch (ParseException e) {
                return new Date();
            }
        }
        return new Date();
    }

    private void refreshArrayList() {
        ArrayList tempList = diaryDB.getAllmyObjects();
        entries.clear();
        entries.addAll(tempList);
        diary_adapter.notifyDataSetChanged();
    }

    private void initDatabase() {
        diaryDB = new DiaryDbDatabase(this);
        diaryDB.open();
    }

    private void initEntryList() {
        entries = new ArrayList<DiaryEntry>();
        initListAdapter();
    }

    private void initListAdapter() {
        ListView list = (ListView) findViewById(R.id.diary_list);
        diary_adapter = new DiaryAdapter(this, entries);
        list.setAdapter(diary_adapter);
    }


    private void setupUI() {
        Button newEntry = (Button) findViewById(R.id.newEntry_Button);
        newEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Diary_Menu_Activity.this, Diary_Entry_Activity.class);
                startActivity(intent);
            }
        });
        ListView diarylist = (ListView) findViewById(R.id.diary_list);
        diarylist.setEmptyView(findViewById(R.id.empty_text));
    }


    protected void onDestroy() {
        super.onDestroy();
        diaryDB.close();
    }


    /*
        Method to clear all database entries.
     */
    private void deleteDiaryEntries() {
        // Probably good idea to implement an alert dialog
        diaryDB.clearDatabase(diaryDB.DIARY_TABLE);
        refreshArrayList();
        Toast.makeText(Diary_Menu_Activity.this,R.string.diary_deleted_toast, Toast.LENGTH_LONG).show();
    }

}
