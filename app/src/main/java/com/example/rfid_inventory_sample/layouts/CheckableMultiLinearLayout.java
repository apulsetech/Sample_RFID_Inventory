package com.example.rfid_inventory_sample.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.rfid_inventory_sample.R;

public class CheckableMultiLinearLayout extends LinearLayout implements Checkable {

    public CheckableMultiLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableMultiLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CheckableMultiLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CheckableMultiLinearLayout(Context context) {
        super(context);
    }

    @Override
    public void setChecked(boolean checked) {
        CheckBox checkBox = findViewById(R.id.checkbox);
        if (checkBox.isChecked() != checked) {
            checkBox.setChecked(checked);
        }
    }

    @Override
    public boolean isChecked() {
        CheckBox checkBox = findViewById(R.id.checkbox);
        return checkBox.isChecked();
    }

    @Override
    public void toggle() {
        CheckBox checkBox = findViewById(R.id.checkbox);
        setChecked(checkBox.isChecked() ? false : true);
    }
}
