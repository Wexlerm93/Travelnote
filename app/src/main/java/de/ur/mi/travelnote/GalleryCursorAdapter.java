
 package de.ur.mi.travelnote;

 import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;


 public class GalleryCursorAdapter extends CursorAdapter {

    Context context;

     public GalleryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        this.context = context;
     }

     public void bindView(View view, Context context, Cursor cursor) {
         ImageView image = (ImageView) view.findViewById(R.id.image_view);
         image.setImageBitmap(stringToBitmap(cursor.getString(1)));
     }

     public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(R.layout.listview_gallery, parent, false);
     }


     public final static Bitmap stringToBitmap(String in){
         Bitmap myBitmap = BitmapFactory.decodeFile(in);
         return myBitmap;
     }
 }


