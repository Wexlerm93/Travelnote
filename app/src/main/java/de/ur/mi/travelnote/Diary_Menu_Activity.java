package de.ur.mi.travelnote;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

/**
 * Created by wexle on 16.08.2017.
 */

public class Diary_Menu_Activity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_menu);
        setupUI();
        setuplist();
    }

    private void setuplist() {
        ListView diarylist = (ListView) findViewById(R.id.diary_list);
        diarylist.setEmptyView(findViewById(R.id.empty_text));
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

    }


}
