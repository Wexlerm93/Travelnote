package de.ur.mi.travelnote;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
        final EditText editContent = (EditText) findViewById(R.id.content_edit);
        final EditText editDate =  (EditText) findViewById(R.id.date_edit);
        final EditText editPlace = (EditText) findViewById(R.id.place_edit);
        editDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                showDatePickerDialog();
            }
        });
        Button addButton = (Button) findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (editDate.getText() != null && editDate.getText().toString() != "" && editContent.getText() != null && editPlace.getText() != null ) {
                    Intent intent = new Intent(Diary_Entry_Activity.this, Diary_Menu_Activity.class);
                    intent.putExtra("content", editContent.getText().toString());
                    intent.putExtra("place", editPlace.getText().toString());
                    intent.putExtra("date", editDate.getText().toString());
                    startActivity(intent);
                }   else {
                    Toast.makeText(Diary_Entry_Activity.this, "Es müssen alle Felder ausgefüllt werden!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void showDatePickerDialog() {
        DialogFragment dateFragment = new DatePickerFragment();
        dateFragment.show(getFragmentManager(), "datePicker");

    }

}
