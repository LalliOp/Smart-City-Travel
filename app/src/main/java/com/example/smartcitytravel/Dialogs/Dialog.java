package com.example.smartcitytravel.Dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.smartcitytravel.R;

public class Dialog extends DialogFragment {
    private String title;
    private String message;
    private View dialogView;

    public Dialog(String title, String message) {
        this.title = title;
        this.message = message;
    }

    @NonNull
    @Override
    public AlertDialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.error_dialog, null);

        setTitle();
        setMessage();
        return createDialog();

    }

    public void setTitle() {
        TextView titleTxt = dialogView.findViewById(R.id.titleTxt);
        titleTxt.setText(title);
    }

    public void setMessage() {
        TextView messageTxt = dialogView.findViewById(R.id.messageTxt);
        messageTxt.setText(message);
    }

    public AlertDialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}
