package com.example.rfid_inventory_sample.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.example.rfid_inventory_sample.R;

import java.util.ArrayList;
import java.util.Locale;

public class PowerGainDialog {
    private static final String TAG = PowerGainDialog.class.getSimpleName();

    public static void show(Context context, int minPower, int maxPower, int powerGain,
                            OnSelectPowerGainListener listener) {
        int selectIndex = 0;
        int index = 0;
        ArrayList<CharSequence> powerList = new ArrayList<>();
        final ArrayList<Integer> powerLevels = new ArrayList<>();
        final ArrayList<Integer> seelctedPosition = new ArrayList<>();

        for (int i = maxPower; i >= minPower; i--, index++) {
            if (i == powerGain) {
                selectIndex = index;
            }
            powerList.add(String.format(Locale.US, "%d.0 dBm", i));
            powerLevels.add(i);
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.power_level_title);
        builder.setSingleChoiceItems(powerList.toArray(new CharSequence[powerList.size()]),
                selectIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        seelctedPosition.clear();
                        seelctedPosition.add(position);
                    }
                });
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.action_select, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (seelctedPosition.size() > 0) {
                    int position = seelctedPosition.get(0);
                    int powerGain = powerLevels.get(position);
                    if (listener != null)
                        listener.onSelectPowerGain(powerGain);
                }
            }
        });
        builder.setNegativeButton(R.string.action_cancel, null);
        builder.show();

        Log.i(TAG, "INFO. show()");
    }

    public interface OnSelectPowerGainListener {
        void onSelectPowerGain(int powerGain);
    }
}
