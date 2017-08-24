package de.ur.mi.travelnote;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by wexle on 16.08.2017.
 */

public class Diary_Entry_Activity extends Activity {



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_entry_activity);
        setupUI();
    }

    private void setupUI() {
        TextView currentItem = (TextView) findViewById(R.id.currentItem);
        final EditText editContent = (EditText) findViewById(R.id.content_edit);
        final EditText editDate =  (EditText) findViewById(R.id.date_edit);
        editDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                showDatePickerDialog();
            }
        });
        Button addButton = (Button) findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Diary_Entry_Activity.this, Diary_Menu_Activity.class);
                intent.putExtra("content", editContent.getText().toString());
                intent.putExtra("date", editDate.getText().toString());
                startActivity(intent);
            }
        });

    }

    private void showDatePickerDialog() {
        DialogFragment dateFragment = new DatePickerFragment();
        dateFragment.show(getFragmentManager(), "datePicker");

    }

}
