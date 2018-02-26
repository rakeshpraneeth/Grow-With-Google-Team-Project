package com.madonasyombua.growwithgoogleteamproject.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.madonasyombua.growwithgoogleteamproject.MainActivity;
import com.madonasyombua.growwithgoogleteamproject.adapter.FragmentsAdapter;
import com.madonasyombua.growwithgoogleteamproject.ProfileActivity;
import com.madonasyombua.growwithgoogleteamproject.R;
import com.madonasyombua.growwithgoogleteamproject.databinding.ActivityLoginBinding;
import com.madonasyombua.growwithgoogleteamproject.ui.fragment.LoginFragment;
import com.madonasyombua.growwithgoogleteamproject.ui.fragment.RegisterFragment;
import com.madonasyombua.growwithgoogleteamproject.ui.intro.OnBoardingActivity;

import java.util.Arrays;
// I see we have a jonathanfinerty import here, I hope we can get details on it
import jonathanfinerty.once.Once;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    CallbackManager mCallbackManager;

    // Facebook permissions - public profile, email
    // TODO: Add more permissions as needed and make sure to add it to the permission array list below
    private static final String PUBLIC_PROFILE_PERMISSION = "public_profile";
    private static final String EMAIL_PERMISSION = "email";

    private ActivityLoginBinding binding;
    private static final String TAG = "LoginActivity";
    static final int SHOW_INTRO = 1;

    GoogleSignInClient mGoogleSignInClient;
    private static final int GOOGLE_SIGN_IN_REQUEST_CODE = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        mCallbackManager = CallbackManager.Factory.create();

        // Binding and listening to all the buttons
        binding.btnLogin.setOnClickListener(this);
        binding.btnRegister.setOnClickListener(this);
        binding.btnFacebookLogin.setOnClickListener(this);
        binding.btnGoogleLogin.setOnClickListener(this);

        // Buttons initial state
        binding.btnLogin.setBackgroundResource(R.drawable.button_rounded_focused);
        binding.btnRegister.setBackgroundResource(R.drawable.button_rounded_normal);

        //this will set login fragment as the first thing you see
        setViewPager(binding.container);

        // Google login set up
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestEmail()
                                        .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getBaseContext(), R.string.facebook_login_success, Toast.LENGTH_LONG).show();
                Log.d(TAG, "Facebook user id: " + loginResult.getAccessToken().getUserId());
                Log.d(TAG, "Facebook access token: " + loginResult.getAccessToken().getToken());

                // TODO: Use the returned token from loginResult to make a graph API request for user info (name, email, ....)
                // Redirect user to the MainActivity
                Intent mainActivityIntent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(mainActivityIntent);
            }

            @Override
            public void onCancel() {
                Toast.makeText(getBaseContext(), R.string.facebook_login_cancel, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getBaseContext(), R.string.facebook_login_error, Toast.LENGTH_LONG).show();
            }
        });

        //setup the onboarding activity
        Once.initialise(this);
        if (!Once.beenDone(Once.THIS_APP_INSTALL, "showTutorial")) {
            Log.i(TAG, "onCreate: First time");
            startActivityForResult(new Intent(this, OnBoardingActivity.class), SHOW_INTRO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SHOW_INTRO) {
            if (resultCode == RESULT_OK) {
                Once.markDone("showTutorial");
            }
        } else if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()){

            mCallbackManager.onActivityResult(requestCode, resultCode, data);

        } else if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {

            Task<GoogleSignInAccount> gTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(gTask);
        }
    }


    private void setViewPager(ViewPager viewPager) {
        FragmentsAdapter adapter = new FragmentsAdapter(getSupportFragmentManager());
        adapter.addFragment(new LoginFragment());
        viewPager.setAdapter(adapter);
    }

    private void registerFragment() {
        FragmentsAdapter adapter = new FragmentsAdapter(getSupportFragmentManager());
        adapter.addFragment(new RegisterFragment());
        binding.container.setAdapter(adapter);
    }

    /***
     * Handle login process when the login button is pressed
     */
    private void handleLoginProcess() {
        // Toggle buttons
        binding.btnLogin.setBackgroundResource(R.drawable.button_rounded_focused);
        binding.btnRegister.setBackgroundResource(R.drawable.button_rounded_normal);

        Toast.makeText(LoginActivity.this, "Going to login fragment", Toast.LENGTH_SHORT).show();
        setViewPager(binding.container);

    }

    /**
     * Handle registration process when the register button is clicked
     *
     * @param
     * @return
     */
    private void handleRegistrationProcess() {
        // Toggle buttons
        binding.btnLogin.setBackgroundResource(R.drawable.button_rounded_normal);
        binding.btnRegister.setBackgroundResource(R.drawable.button_rounded_focused);

        Toast.makeText(LoginActivity.this, "Going to register fragment", Toast.LENGTH_SHORT).show();
        registerFragment();

    }


    /***
     * Handle facebook log in
     *
     * @param
     * @return
     */
    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList(PUBLIC_PROFILE_PERMISSION, EMAIL_PERMISSION));
    }

    /***
     * Handle google sign in process
     *
     * @param
     * @return
     */
    private void signInWithGoogle() {

        // Create a google sign in intent
        Intent googleSignInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(googleSignInIntent, GOOGLE_SIGN_IN_REQUEST_CODE);
    }

    /***
     * Handles google sign in result
     *
     * @param completedTask - completed GoogleSignInAccount task
     * @return
     */
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {

        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Successfully signed in
            if (account != null) {

                // TODO: All user's basic info such as name can now be obtained from the account object
                String personName = account.getDisplayName();
                String personEmail = account.getEmail();

                // For debugging purposes only
                Log.d(TAG, "Google info " + personName + " - " + personEmail);

                // Start MainActivity
                Intent mainActivityIntent = new Intent(getBaseContext(), MainActivity.class);

                // TODO: User info can be passed in to the MainActivity as EXTRA or stored locally in db or Firebase
                startActivity(mainActivityIntent);
            }

        } catch (ApiException e) {

            // Google sign in failed
            e.printStackTrace();
        }

    }

    /***
     * Detect click events for all the different views (buttons)
     *
     * @param v - view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_register:
                handleRegistrationProcess();
                break;
            case R.id.btn_login:
                handleLoginProcess();
                break;

            case R.id.btn_facebook_login:
                signInWithFacebook();
                break;

            case R.id.btn_google_login:
                signInWithGoogle();
                break;
        }

    }
}
