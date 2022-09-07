package com.apulsetech.sample.bluetooth.rfid.inventory_sample;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.apulsetech.lib.provider.LocalStateDBHelper;
import com.apulsetech.sample.bluetooth.rfid.inventory_sample.types.CustomContext;

public class SampleApplication extends Application {
    private static final String TAG = SampleApplication.class.getSimpleName();

    private LocalStateDBHelper mStateDBHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        LocalStateDBHelper.setAuthorities("com.apulsetech.sample.bluetooth.rfid.inventory_sample.LocalStateProvider");
        mStateDBHelper =
                new LocalStateDBHelper(this);
        mStateDBHelper.reset();

        registerReceiver(mApplicationReceiver,
                new IntentFilter(CustomContext.ACTION_REMOTE_CONNECTION_REQUESTED));
    }

    @Override
    public void onTerminate() {
        mStateDBHelper.reset();
        unregisterReceiver(mApplicationReceiver);
        super.onTerminate();
    }

    private final BroadcastReceiver mApplicationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int rfidRunState = mStateDBHelper.getRfidRunState();
            boolean rfidRunning = rfidRunState != 0;

            if (rfidRunning) {
                Intent baseActivityIntent =
                        new Intent(SampleApplication.this,
                                MainActivity.class);
                baseActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(baseActivityIntent);
            }
        }
    };
}
