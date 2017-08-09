package de.ur.mi.travelnote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    LoginButton loginButton;
    TextView textView;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupLoginButton();



    }

    /* Method to set up a facebook login button
        Currently the method does not very much.

     */
    private void setupLoginButton() {
        loginButton = (LoginButton)findViewById(R.id.login_button);
        textView = (TextView)findViewById(R.id.statusText);
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            //Success method: Defines what happens if facebook login was successful and permission is granted
            @Override
            public void onSuccess(LoginResult loginResult) {
                textView.setText("Login erfolgreich \n" + loginResult.getAccessToken().getUserId() + "\n"+loginResult.getAccessToken().getToken());
            }

            //Cancel method: defines what happens if facebook login is canceled or permission is not granted
            @Override
            public void onCancel() {
                textView.setText("Login abgebrochen");
            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }
}

