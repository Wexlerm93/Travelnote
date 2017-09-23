package de.ur.mi.travelnote;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;



public class GalleryBaseAdapter extends BaseAdapter {

    private Context context;
    private  int layout;
    private ArrayList<CustomImage> imageList;

    public GalleryBaseAdapter(Context context, int layout, ArrayList<CustomImage> imageList) {
        this.context = context;
        this.layout = layout;
        this.imageList = imageList;
    }


    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object getItem(int position) {
        return imageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    private class ViewHolder{
        ImageView imageView;
        TextView txtName, txtPrice;
    }




    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        View row = view;
        ViewHolder holder = new ViewHolder();

        if(row == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layout, null);
            holder.imageView = (ImageView) row.findViewById(R.id.gallery_image);
            holder.txtName = (TextView) row.findViewById(R.id.gallery_image_title);
            holder.txtPrice = (TextView) row.findViewById(R.id.gallery_image_location);
            row.setTag(holder);
        }
        else {
            holder = (ViewHolder) row.getTag();
        }

        CustomImage image = imageList.get(position);

        holder.txtName.setText(image.getTitle());
        holder.txtPrice.setText(image.getLocation());

        byte[] foodImage = image.getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(foodImage, 0, foodImage.length);
        holder.imageView.setImageBitmap(bitmap);

        return row;
    }

}
