package de.ur.mi.travelnote;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    /*
        This activity is for login purposes.
        It uses the external library and service Firebase, which is provided by Google.

        This activity handles several login opportunities and also checks if user already is logged-in, so that he gets directly to StartActivity
     */

    private FirebaseAuth auth;
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get a Instance of Firebase Authentication Service
        auth = FirebaseAuth.getInstance();

        if(isOnline()){
            //check if user is already logged-in --> redirect user to StartActivity, if not: handle log in or registration process
            if(auth.getCurrentUser() != null){
                //user already signed in
                redirectToStart();
            }else{
                // user has to authenticate.. handle registration or log-in process and provide result
                startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                        //new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                        new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                        .setTheme(R.style.LoginTheme).build(), RC_SIGN_IN);
            }
        }else{
            TextView noConnectionText = (TextView) findViewById(R.id.no_conn_text);
            TextView noConnectionTextDetail = (TextView) findViewById(R.id.no_conn_text_detail);
            noConnectionText.setText("Keine Internetverbindung.");
            noConnectionTextDetail.setText("Bitte überprüfe Deine Internetverbindung und starte die App erneut.");
        }

    }


    // now handle result from registration or log-in process
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                //user logged in
                redirectToStart();
            }else{
                // user not authenticated
                Toast.makeText(this, "Login nicht möglich. \n Versuchen Sie es später erneut!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // building intent to get to StartActivity
    private void redirectToStart() {
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}

