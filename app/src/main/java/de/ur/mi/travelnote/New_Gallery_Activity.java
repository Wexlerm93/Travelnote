package de.ur.mi.travelnote;


import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wexle on 09.09.2017.
 */

public class New_Gallery_Activity extends AppCompatActivity{

    private static int RESULT_LOAD_IMAGE = 1;
    String imageEncode;
    List<String> imagesEncodedList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_gallery_layout);
        setupUi();

    }

    private void setupUi() {

        Button addImage = (Button) findViewById(R.id.add_Pictures_Button);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent();
                photoPickerIntent.setType("image/*");
                photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(photoPickerIntent, "Select Picture"), RESULT_LOAD_IMAGE);
            }
        });

    }

    public final static Bitmap stringToBitmap(String in){
        Bitmap myBitmap = BitmapFactory.decodeFile(in);
        return myBitmap;
    }

    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        try {
            //When an Image is picked
            if (reqCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                imagesEncodedList = new ArrayList<String>();
                if (data.getData() != null){

                    Uri mImageUri = data.getData();

                    //Get the cursor
                    Cursor cursor = getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    //Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncode = cursor.getString(columnIndex);
                    cursor.close();
                    Picture(MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri));
                    setText(1);
                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> myArrayUri = new ArrayList<Uri>();
                        for (int i = 0; i <mClipData.getItemCount(); i++) {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            myArrayUri.add(uri);

                            //Get the cursor
                            Cursor cursor = getContentResolver().query(uri,
                                    filePathColumn, null, null, null);
                            //Move to first row
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncode = cursor.getString(columnIndex);
                            imagesEncodedList.add(imageEncode);
                            cursor.close();
                        }
                        setText(mClipData.getItemCount());
                        Picture(MediaStore.Images.Media.getBitmap(this.getContentResolver(), myArrayUri.get(1)));

                    }
                }
            } else {
                Toast.makeText(this, "Kein Bild ausgewählt", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }

        super.onActivityResult(reqCode, resultCode, data);
    }

    public void setText(int count){
        TextView numberOfPictures = (TextView) findViewById(R.id.count);
        numberOfPictures.setText(String.valueOf(count));
    }

    public void Picture(Bitmap picture) {
        ImageView newPicture = (ImageView) findViewById(R.id.test_picture);
        newPicture.setImageBitmap(picture);
    }
}
