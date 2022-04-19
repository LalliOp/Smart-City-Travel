package com.example.smartcitytravel.Activities.ResetPassword;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.Activities.PinCode.PinCodeActivity;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Color;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.Util.Validation;
import com.example.smartcitytravel.databinding.ActivityEmailBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmailActivity extends AppCompatActivity {
    private ActivityEmailBinding binding;
    private Util util;
    private Connection connection;
    private Color color;
    private Validation validation;
    private boolean validate_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initialize();
        util.setStatusBarColor(EmailActivity.this, R.color.black);
        setLoadingBarColor();
        continueButtonClickListener();
    }

    //initialize variables
    public void initialize() {
        util = new Util();
        connection = new Connection();
        color = new Color();
        validation = new Validation();
        validate_email = false;
    }

    // call when user click on continue button
    // check email ,send pin code and move to pin code activity
    public void continueButtonClickListener() {
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                util.hideKeyboard(EmailActivity.this);
                validateEmail();
                if (validate_email) {
                    checkConnectionAndVerifyEmail();
                }


            }
        });
    }

    //check email field contain valid and allowed characters
    public void validateEmail() {
        String email = binding.emailEdit.getText().toString();
        String errorMessage = validation.validateEmail(email);
        if (errorMessage.isEmpty()) {
            hideEmailError();

        } else {
            showEmailError(errorMessage);
        }
    }

    //show error msg and error icon color in email field
    public void showEmailError(String errorMsg) {
        binding.emailLayout.setErrorIconTintList(color.iconRedColor(this));
        binding.emailLayout.setError(errorMsg);
        validate_email = false;
    }

    //hide error icon color and msg in email field when no error occurs
    public void hideEmailError() {
        binding.emailLayout.setError(null);
        validate_email = true;
    }

    // check internet connection exist ot not. If exist than verify email by database
    public void checkConnectionAndVerifyEmail() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(EmailActivity.this);
        if (isConnectionSourceAvailable) {
            showLoadingBar();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean internetAvailable = connection.isInternetAvailable();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (internetAvailable) {
                            verifyEmail();
                        } else {
                            Toast.makeText(EmailActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            hideLoadingBar();
                        }
                    }
                });

            }
        });
    }

    //check whether email exist or not
    public void verifyEmail() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").whereEqualTo("email", binding.emailEdit.getText().toString().toLowerCase())
                .get()
                .addOnSuccessListener(this,new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                boolean google_account = querySnapshot.getBoolean("google_account");
                                if (google_account) {
                                    util.createErrorDialog(EmailActivity.this, "Account", "Account exist with google. Google account password can only change through google");
                                } else {
                                    moveToPinCodeActivity();
                                }
                            }
                        } else {
                            util.createErrorDialog(EmailActivity.this, "Account", "No account exist with this email");
                        }
                        hideLoadingBar();
                    }
                });
    }

    //change default loading bar color
    public void setLoadingBarColor() {
        ColorStateList colorStateList = ColorStateList.valueOf(getResources().getColor(R.color.light_orange_2));
        binding.loadingProgressBar.loadingBar.setIndeterminateTintList(colorStateList);
    }

    // show progress bar when user click on continue button
    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(EmailActivity.this);

    }

    //hide progressbar when move to next activity or error occurs
    public void hideLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(EmailActivity.this);

    }

    //Move from Email Activity to Pin Code Activity
    public void moveToPinCodeActivity() {
        Intent intent = new Intent(this, PinCodeActivity.class);
        intent.putExtra("email", binding.emailEdit.getText().toString());
        intent.putExtra("title", "Reset Your Password");
        startActivity(intent);
    }

}