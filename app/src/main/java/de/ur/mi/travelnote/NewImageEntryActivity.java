package de.ur.mi.travelnote;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class NewImageEntryActivity extends AppCompatActivity {

    private EditText title, location;
    private Button chooseButton, addButton;
    private ImageView imageView;

    String userID;

    private final int REQUEST_CODE_GALLERY = 654;

    public static ImageDBHelper imageDBHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_gallery_entry);
        setupToolbar();
        initUIFields();
        getUserInfo();
        imageDBHelper = new ImageDBHelper(this);
        getImageFromFileSystemImage();
        getImageFromFileSystemButton();
        addImage();
    }

    private void setupToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_new_gallery);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(R.string.new_gallery_toolbar_text);
        }
    }

    private void getUserInfo(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        }
    }

    private void getImageFromFileSystemImage(){
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(
                        NewImageEntryActivity.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_GALLERY
                );
            }
        });
    }

    private void getImageFromFileSystemButton(){
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(
                        NewImageEntryActivity.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_GALLERY
                );
            }
        });
    }

    private void addImage(){
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sTitle = title.getText().toString();
                String sLocation = location.getText().toString();
                if(sTitle.equals("") || sLocation.equals("")){
                    Toast.makeText(NewImageEntryActivity.this, "Bitte fülle alle Felder aus!", Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        byte[] image = imageViewToByte(imageView);
                        imageDBHelper.insertData(sTitle, sLocation, image, userID);
                        clearFields();
                        clearLatestState();
                        imageView.setImageResource(R.drawable.ow_default_image);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static byte[] imageViewToByte(ImageView image) {
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            } else {
                Toast.makeText(getApplicationContext(), "Travelnote scheint nicht auf Deine Bilder zugfreifen zu dürfen.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {


            Uri uri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);

                /*
                That's how we would get the actual file path to image to store in db and fetch from db afterwards
                That would make the app much more responsive

                Uri imageUri = getImageUri(getApplicationContext(), bitmap);
                File file = new File(getRealPathFromURI(imageUri));
                String filePath = file.toString();
                */

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
        Methods getImageUri and getRealPathFromURI are from Siddharth Lele
        (see https://stackoverflow.com/questions/15432592/get-file-path-of-image-on-android?answertab=votes#tab-top), last retrieved on 2017-09-23
     */

    /*
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor getFilePathCursor = getContentResolver().query(uri, null, null, null, null);
        getFilePathCursor.moveToFirst();
        int idx = getFilePathCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return getFilePathCursor.getString(idx);
    }
    */

    private void initUIFields() {
        title = (EditText) findViewById(R.id.image_title_input);
        location = (EditText) findViewById(R.id.image_location_input);
        chooseButton = (Button) findViewById(R.id.choose_button);
        addButton = (Button) findViewById(R.id.buttonAdd);
        imageView = (ImageView) findViewById(R.id.selected_image);
    }

    //Setups toolbar's options menu and inflates the layout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_buttons_diary_entry_menu, menu);
        return true;
    }

    //Override method for what to do, if item from toolbar's options menu is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_close_new_entry) {
            clearFields();
            clearLatestState();
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void clearFields(){
        title.setText("");
        location.setText("");
    }

    /*
        SharedPreferences Methods
        Save the current values of the editText fields, when method is called (in onPause method)
     */

    private void saveCurrentState() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("title", title.getText().toString());
        editor.putString("location", location.getText().toString());
        editor.apply();
    }

    //method do load saved preferences in onResume (when user gets back to activity)
    private void loadLatestState() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String sTitle = sharedPreferences.getString("title", "");
        String sLocation = sharedPreferences.getString("location", "");
        title.setText(sTitle);
        location.setText(sLocation);
    }

    //method to clear SharedPreferences entries
    private void clearLatestState() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().apply();
    }

}
