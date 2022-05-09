package com.example.smartcitytravel.Activities.ChangePassword;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.DataModel.User;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.Util.Validation;
import com.example.smartcitytravel.databinding.ActivityChangePasswordBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePasswordActivity extends AppCompatActivity {
    private ActivityChangePasswordBinding binding;
    private PreferenceHandler preferenceHandler;
    private Validation validation;
    private Util util;
    private boolean validate_old_password;
    private boolean validate_new_password;
    private boolean validate_confirm_new_password;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceHandler = new PreferenceHandler();
        validation = new Validation();
        util = new Util();
        user = preferenceHandler.getLoggedInAccountPreference(this);

        validate_old_password = false;
        validate_new_password = false;
        validate_confirm_new_password = false;

        save();

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

    // save new password in database
    public void updateNewPassword() {
        if (validate_new_password && validate_old_password && validate_confirm_new_password) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("user")
                    .document(user.getUserId())
                    .update("password", binding.newPasswordEdit.getText().toString())
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            preferenceHandler.updatePasswordPreference(binding.newPasswordEdit.getText().toString(), ChangePasswordActivity.this);
                            Toast.makeText(ChangePasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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
                updateNewPassword();
            }
        });
    }
}