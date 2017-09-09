package de.ur.mi.travelnote;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by wexle on 09.09.2017.
 */

public class New_Gallery_Activity extends AppCompatActivity{

    private static int RESULT_LOAD_IMAGE = 1;
    Bitmap pictureList;

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
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);
            }
        });

    }

    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStram = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStram);
                Picture(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(New_Gallery_Activity.this, "Kein Bild ausgewählt", Toast.LENGTH_LONG).show();
        }
    }

    public void Picture(Bitmap picture) {
        ImageView newPicture = (ImageView) findViewById(R.id.test_picture);
        newPicture.setImageBitmap(picture);
    }
}
