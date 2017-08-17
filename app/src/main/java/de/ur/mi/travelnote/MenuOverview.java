package de.ur.mi.travelnote;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by wexle on 09.08.2017.
 */

public class MenuOverview extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_overview);
        setupMenu();
    }

    private void setupMenu() {
        Button map = (Button) findViewById(R.id.map_button);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(MenuOverview.this, Map.class);
                startActivity(intent);
            }
        });
        Button diary = (Button) findViewById(R.id.diary_button);
        diary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuOverview.this, Diary_Menu_Activity.class);
                startActivity(intent);
            }
        });
    }
}
