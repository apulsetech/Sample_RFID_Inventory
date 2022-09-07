package com.apulsetech.sample.bluetooth.rfid.inventory_sample.dialogs;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.apulsetech.sample.bluetooth.rfid.inventory_sample.R;

import java.util.ArrayList;
import java.util.List;

public class SingleNameValueListDialog {
    public static void show(Context context, int title, int value,
                            String[] names, int[] values,
                            OnSelectValueListener listener) {
        final List<Integer> selectPosition = new ArrayList<>();
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        int index = -1;

        for (int i = 0; i < values.length; i++) {
            if (value == values[i]) {
                index = i;
                break;
            }
        }

        builder.setTitle(title);
        builder.setSingleChoiceItems((CharSequence[]) names, index,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                selectPosition.clear();
                selectPosition.add(position);
            }
        });
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.action_select, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectPosition.size() > 0) {
                    int position = selectPosition.get(0);
                    if (listener != null)
                        listener.onSelectValue(values[position]);
                }
            }
        });
        builder.setNegativeButton(R.string.action_cancel, null);
        builder.show();
    }

    public interface OnSelectValueListener {
        void onSelectValue(int value);
    }
}
