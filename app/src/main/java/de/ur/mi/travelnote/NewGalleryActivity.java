package de.ur.mi.travelnote;


import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.ur.mi.travelnote.de.ur.mi.travelnote.sqlite.helper.DatabaseHelper;

/**
 * Created by wexle on 09.09.2017.
 */

public class NewGalleryActivity extends AppCompatActivity{

    private static int RESULT_LOAD_IMAGE = 1;
    String imageEncode;
    List<String> imagesEncodedList;
    ArrayList<Uri> myUriList;

    private DatabaseHelper mDatabaseHelper;
    private String userID;
    private String userName;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_gallery_layout);
        getUserInfo();
        Toolbar toolbar= (Toolbar) findViewById(R.id.basic_toolbar);
        setSupportActionBar(toolbar);
        setupUi();
        mDatabaseHelper = new DatabaseHelper(this);
    }

    /*
        Method to create option menu
     */

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_buttons_diary_entry_menu, menu);
        return true;
    }

    /*
        Method to do task for selected menu item
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_close_new_entry){
            finish();
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    private void getUserInfo(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userID = user.getUid();
            userName = user.getDisplayName();
        }
    }

    private void setupUi() {

        Button chooseImage = (Button) findViewById(R.id.choose_Pictures_Button);
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent();
                photoPickerIntent.setType("image/*");
                photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(photoPickerIntent, "Select Picture"), RESULT_LOAD_IMAGE);
            }
        });

        final Button addImages = (Button) findViewById(R.id.add_pics_btn);
        addImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myUriList == null) {
                    displayShortToast(R.string.no_pic_chosen);
                } else {
                    try {
                        addImagesToDb(myUriList);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    /*
        Method runs through the in "onActivityResult" filled myUriList and adds the images to the Database
     */

    private void addImagesToDb(ArrayList<Uri> myUriList) throws IOException {
        if (!myUriList.isEmpty()) {
            for (int i = 0; i < myUriList.size(); i++) {
                boolean insert = mDatabaseHelper.addImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), myUriList.get(i)));

                if (insert) {
                    displayShortToast(R.string.entry_successful_toast);

                } else {
                    displayShortToast(R.string.failed_saving_entry);
                }
            }
            myUriList.clear();
            TextView count = (TextView) findViewById(R.id.image_count);
            count.setText(mDatabaseHelper.getCount());
        } else{
            displayShortToast(R.string.no_pic_chosen);
        }
    }

    /*
        Method opens the users gallery an the can select multiple images. Upon returning the images are saved to myUriList
        One images is always displayed (this can be deleted later)
     */

    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        try {
            //When an Image is picked
            if (reqCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                imagesEncodedList = new ArrayList<String>();
                myUriList = new ArrayList<Uri>();
                if (data.getData() != null){

                    Uri mImageUri = data.getData();
                    myUriList.add(mImageUri);

                    //Get the cursor
                    Cursor cursor = getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    //Move to first row
                    cursor.moveToFirst();
                    Picture(MediaStore.Images.Media.getBitmap(this.getContentResolver(), myUriList.get(0)));
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncode = cursor.getString(columnIndex);
                    cursor.close();
                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        myUriList = new ArrayList<Uri>();
                        for (int i = 0; i <mClipData.getItemCount(); i++) {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            myUriList.add(uri);

                            //Get the cursor
                            Cursor cursor = getContentResolver().query(uri,
                                    filePathColumn, null, null, null);
                            //Move to first row
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncode = cursor.getString(columnIndex);
                            imagesEncodedList.add(imageEncode);
                            cursor.close();
                            Picture(MediaStore.Images.Media.getBitmap(this.getContentResolver(), myUriList.get(0)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }

        super.onActivityResult(reqCode, resultCode, data);
    }

    /*
        Method to show one picture
     */
    public void Picture(Bitmap picture) {
        ImageView newPicture = (ImageView) findViewById(R.id.test_picture);
        newPicture.setImageBitmap(picture);
    }

    private void displayShortToast(int id) {
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
    }


}
