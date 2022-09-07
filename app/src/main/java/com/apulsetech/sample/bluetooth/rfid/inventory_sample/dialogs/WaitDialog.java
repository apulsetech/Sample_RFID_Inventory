package com.apulsetech.sample.bluetooth.rfid.inventory_sample.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.apulsetech.sample.bluetooth.rfid.inventory_sample.R;

public final class WaitDialog {
    private static final String TAG = WaitDialog.class.getSimpleName();

    private static WaitProgressDialog mDialog = null;

    public static void show(Context context) {
        if (mDialog != null)
            return;
        mDialog = new WaitProgressDialog(context);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setCancelable(false);
        mDialog.show();

        Log.i(TAG, "INFO. show()");
    }

    public static void show(Context context, String msg) {
        if (mDialog != null)
            return;
        mDialog = new WaitProgressDialog(context, msg);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setCancelable(false);
        mDialog.show();

        Log.i(TAG, "INFO. show()");
    }

    public static void hide() {
        if (mDialog == null)
            return;
        mDialog.dismiss();
        mDialog = null;

        Log.i(TAG, "INFO. hide()");
    }

    private static class WaitProgressDialog extends Dialog implements DialogInterface.OnShowListener {
        private String mMessage;

        public WaitProgressDialog(Context context) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_wait_progress);
            mMessage = null;
        }

        public WaitProgressDialog(Context context, String msg) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_wait_progress_message);
            mMessage = msg;
            this.setOnShowListener(this);
        }

        public String getMessage() {
            return mMessage;
        }

        @Override
        public void onShow(DialogInterface dialog) {
            if (mMessage != null) {
                TextView txtMsg = findViewById(R.id.message);
                txtMsg.setText(mMessage);
            }
        }
    }
}
