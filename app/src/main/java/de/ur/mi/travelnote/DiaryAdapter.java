package de.ur.mi.travelnote;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by wexle on 24.08.2017.
 */

public class DiaryAdapter extends ArrayAdapter <DiaryEntry> {

    private ArrayList<DiaryEntry> diaryList;
    private Context context;

    public DiaryAdapter(Context context, ArrayList <DiaryEntry> diaryEntries) {
        super(context, R.layout.diaryentry_list, diaryEntries);
        this.context = context;
        this.diaryList = diaryEntries;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.diaryentry_list, null);

        }

        DiaryEntry item = diaryList.get(position);

        if (item != null) {
            TextView diaryContent = (TextView) v.findViewById(R.id.diary_content);
            TextView diaryDate = (TextView) v.findViewById(R.id.diary_date);

            diaryContent.setText(item.getBody());
            diaryDate.setText(item.getFormattedDate());
        }
        return v;
    }
}




