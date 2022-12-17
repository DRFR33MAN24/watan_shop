package com.sabeal.watanshop.utills;

import static com.airbnb.lottie.L.TAG;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

    }
    @Override
    public void onStop() {
        Log.w(TAG, "App stopped");

        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "App destroyed");

        super.onDestroy();
    }
}
