package com.example.rfid_inventory_sample;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

public class CustomDialog extends Dialog {

    public CustomDialog(Context context){
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_progress);

    }

}
