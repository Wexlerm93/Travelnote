package de.ur.mi.travelnote;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DiaryCursorAdapter extends CursorAdapter {

    Context context;
    long ident;


    public DiaryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        this.context = context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ident = cursor.getLong(0);
        TextView title = (TextView) view.findViewById(R.id.listitem_title);
        title.setText(cursor.getString(1));
        TextView location = (TextView) view.findViewById(R.id.listitem_location);
        location.setText(cursor.getString(2));
        TextView date = (TextView) view.findViewById(R.id.listitem_date);
        date.setText(cursor.getString(5));
        TextView content = (TextView) view.findViewById(R.id.listitem_content);
        content.setText(cursor.getString(2));
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.listview_diary_entries, parent, false);
        return view;
    }

    @Override
    public long getItemId(int position) {
        //return super.getItemId(position);
        return ident;
    }
}
