package com.example.smartcitytravel.Activities.ChangePassword;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.DataModel.User;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.Util.Validation;
import com.example.smartcitytravel.databinding.ActivityChangePasswordBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChangePasswordActivity extends AppCompatActivity {
    private ActivityChangePasswordBinding binding;
    private PreferenceHandler preferenceHandler;
    private Validation validation;
    private Util util;
    private Connection connection;
    private boolean validate_old_password;
    private boolean validate_new_password;
    private boolean validate_confirm_new_password;
    private boolean validate_different_password;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initialize();
        setToolBarTheme();
        save();

    }

    //initialize variables
    public void initialize() {
        preferenceHandler = new PreferenceHandler();
        validation = new Validation();
        util = new Util();
        user = preferenceHandler.getLoggedInAccountPreference(this);
        connection = new Connection();

        validate_old_password = false;
        validate_new_password = false;
        validate_different_password = false;
        validate_confirm_new_password = false;

    }

    // style and customize toolbar and theme
    public void setToolBarTheme() {
        util.setStatusBarColor(this, R.color.theme_light);
        util.addToolbar(this, binding.toolbarLayout.toolbar, "Change Password");
    }

    // check whether existing password is equal to input password
    public void validateOldPassword() {
        if (user.getPassword().equals(binding.oldPasswordEdit.getText().toString())) {
            binding.oldPasswordLayout.setError(null);
            validate_old_password = true;
        } else {
            binding.oldPasswordLayout.setError("Error! Incorrect password");
            binding.oldPasswordLayout.setErrorIconDrawable(null);
            validate_old_password = false;
        }
    }

    //check new password field contain valid and allowed characters
    public void validateNewPassword() {
        String password = binding.newPasswordEdit.getText().toString();
        String errorMessage = validation.validatePassword(password);
        if (errorMessage.isEmpty()) {
            removeNewPasswordError();
        } else {
            showNewPasswordError(errorMessage);
        }
    }

    //show error msg and hide error icon in new password field
    public void showNewPasswordError(String errorMsg) {
        binding.newPasswordLayout.setError(errorMsg);
        binding.newPasswordLayout.setErrorIconDrawable(null);
        validate_new_password = false;
    }

    //hide msg in new password field when no error occurs
    public void removeNewPasswordError() {
        binding.newPasswordLayout.setError(null);
        validate_new_password = true;
    }

    //check confirm new password field
    //and match confirm new password with new password
    public void matchNewPasswordAndConfirmNewPassword() {
        String password = binding.newPasswordEdit.getText().toString();
        String confirmPassword = binding.confirmNewPasswordEdit.getText().toString();
        int errorCode = validation.matchPasswordAndConfirmPassword(password, confirmPassword);

        if (errorCode == 2) {
            showConfirmNewPasswordError("ERROR! Re-enter password");
        } else if (errorCode == 1) {
            showConfirmNewPasswordError("ERROR! Password not matched");
            showNewPasswordError(" ");
        } else if (errorCode == 0) {
            removeConfirmNewPasswordError();
        } else {
            showConfirmNewPasswordError("ERROR! Invalid password");
        }
    }

    //show error msg and hide error icon in confirm new password field
    public void showConfirmNewPasswordError(String errorMsg) {
        binding.confirmNewPasswordLayout.setError(errorMsg);
        binding.confirmNewPasswordLayout.setErrorIconDrawable(null);
        validate_confirm_new_password = false;
    }

    //hide msg in confirm new password field when no error occurs
    public void removeConfirmNewPasswordError() {
        binding.confirmNewPasswordLayout.setError(null);
        validate_confirm_new_password = true;
    }

    // check old password and new password is same or not
    public void validateDifferentPassword() {
        String oldPassword = binding.oldPasswordEdit.getText().toString();
        String newPassword = binding.newPasswordEdit.getText().toString();
        if (newPassword.equals(oldPassword)) {
            util.createErrorDialog(this, "Password Issue", "New password should not be same as old password");
            validate_different_password = false;
        } else {
            validate_different_password = true;
        }

    }

    //check connection exist or not. If exist then update password
    public void checkConnectionAndUpdateNewPassword() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(this);
        if (isConnectionSourceAvailable) {
            showLoadingBar();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean isInternetAvailable = connection.isInternetAvailable();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isInternetAvailable) {
                            updateNewPassword();
                        } else {
                            hideLoadingBar();
                            Toast.makeText(ChangePasswordActivity.this, "Unable to update password", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
        executor.shutdown();
    }

    // save new password in database
    public void updateNewPassword() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user")
                .document(user.getUserId())
                .update("password", binding.newPasswordEdit.getText().toString())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        hideLoadingBar();
                        preferenceHandler.updatePasswordPreference(binding.newPasswordEdit.getText().toString(), ChangePasswordActivity.this);
                        user.setPassword(binding.newPasswordEdit.getText().toString());
                        Toast.makeText(ChangePasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //validate and save record in database
    public void save() {
        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                util.hideKeyboard(ChangePasswordActivity.this);
                validateOldPassword();
                validateNewPassword();
                matchNewPasswordAndConfirmNewPassword();

                if (validate_new_password && validate_old_password && validate_confirm_new_password) {
                    validateDifferentPassword();

                    if (validate_different_password) {
                        checkConnectionAndUpdateNewPassword();
                    }
                }
            }
        });
    }

    // show progress bar
    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(this);
    }

    //hide progressbar
    public void hideLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(this);
    }

    // end this activity when user click on up button (which is back button on top life side)
    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }
}