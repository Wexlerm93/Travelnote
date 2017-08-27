package de.ur.mi.travelnote;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;


public class MainActivity extends AppCompatActivity {

    LoginButton loginButton;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupLoginButton();
        setupStartButton();
    }



    private void setupStartButton() {
        Button startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MenuOverview.class);
                startActivity(intent);
                finish(); // finishes activity, so if user clicks back button in next activity he does not get back to login activity
            }
        });
    }



    /* Method to set up a facebook login button
        Currently the method does not do very much.

        No method what happens (and where) when logging out
        */

    private void setupLoginButton() {
        loginButton = (LoginButton)findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            //Success method: Defines what happens if facebook login was successful and permission is granted
            @Override
            public void onSuccess(LoginResult loginResult) {
                Intent intent = new Intent(MainActivity.this, MenuOverview.class);
                startActivity(intent);
                finish(); // finishes activity, so if user clicks back button in next activity he cannot get back
            }

            //Cancel method: defines what happens if facebook login is canceled or permission is not granted
            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this,R.string.fb_login_cancel_toast, Toast.LENGTH_LONG).show();

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(MainActivity.this,R.string.fb_login_error_toast, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

}

