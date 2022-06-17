/*
 * Copyright (C) Apulsetech,co.ltd
 * Apulsetech, Shenzhen, China
 *
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee is hereby granted, provided that this entire notice is
 * included in all copies of any software which is or includes a copy or
 * modification of this software and in all copies of the supporting
 * documentation for such software.
 *
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY. IN PARTICULAR, NEITHER THE AUTHOR NOR APULSETECH MAKES ANY
 * REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE MERCHANTABILITY OF
 * THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 *
 *
 * Project: ⍺X11 SDK Sample
 *
 * File: SelectMaskActivity.java
 * Date: 2022.06.09
 * Author: John Park, ncsin4@apulsetech.com
 *
 ****************************************************************************
 */

package com.example.rfid_inventory_sample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.apulsetech.lib.event.DeviceEvent;
import com.apulsetech.lib.event.ReaderEventListener;
import com.apulsetech.lib.rfid.Reader;
import com.apulsetech.lib.rfid.type.RFID;
import com.apulsetech.lib.rfid.type.RfidResult;
import com.apulsetech.lib.rfid.type.SelectionCriterias;
import com.example.rfid_inventory_sample.adapters.MaskListAdapter;
import com.example.rfid_inventory_sample.dialogs.MsgBox;
import com.example.rfid_inventory_sample.dialogs.SelectionMaskDialog;
import com.example.rfid_inventory_sample.dialogs.SingleNameValueListDialog;

import java.util.Locale;

public class SelectMaskActivity extends AppCompatActivity implements View.OnClickListener,
        ReaderEventListener, AdapterView.OnItemClickListener, MaskListAdapter.OnCheckedChangeListener {
    private static final String TAG = SelectMaskActivity.class.getSimpleName();

    private CheckBox chkUseSelectMask;
    private TextView txtEntryCount;
    private ListView lstMask;
    private Button btnMaskAdd;
    private Button btnMaskRemove;
    private Button btnMaskClear;
    private TextView txtSession;
    private TextView txtTarget;
    private TextView txtSelectFlag;
    private Button btnSave;
    private Button btnCancel;

    private MaskListAdapter adpMask;

    private Reader mReader = null;

    private String[] sessionNames = null;
    private String[] targetNames = null;
    private String[] selectFlagNames = null;

    private int session = 0;
    private int target = 0;
    private int selectFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mask);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        chkUseSelectMask = findViewById(R.id.use_select_mask);
        txtEntryCount = findViewById(R.id.entry_count);
        lstMask = findViewById(R.id.select_mask);
        adpMask = new MaskListAdapter(this);
        lstMask.setAdapter(adpMask);
        lstMask.setOnItemClickListener(this);
        adpMask.setOnItemCheckedChangeListener(this);
        lstMask.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        btnMaskAdd = findViewById(R.id.action_mask_add);
        btnMaskAdd.setOnClickListener(this);
        btnMaskRemove = findViewById(R.id.action_mask_remove);
        btnMaskRemove.setOnClickListener(this);
        btnMaskClear = findViewById(R.id.action_mask_clear);
        btnMaskClear.setOnClickListener(this);
        txtSession = findViewById(R.id.inventory_option_session);
        txtSession.setOnClickListener(this);
        txtTarget = findViewById(R.id.inventory_option_target);
        txtTarget.setOnClickListener(this);
        txtSelectFlag = findViewById(R.id.inventory_option_select_flag);
        txtSelectFlag.setOnClickListener(this);
        btnSave = findViewById(R.id.action_save);
        btnSave.setOnClickListener(this);
        btnCancel = findViewById(R.id.action_cancel);
        btnCancel.setOnClickListener(this);

        sessionNames = getResources().getStringArray(R.array.session);
        targetNames = getResources().getStringArray(R.array.session_target);
        selectFlagNames = getResources().getStringArray(R.array.selection_flag);

        enableWidgets(false);

        Log.i(TAG, "INFO. onCreate()");
    }

    @Override
    protected void onResume() {
        super.onResume();

        mReader = Reader.getReader(this);
        if (mReader != null) {
            mReader.setEventListener(this);
            updateSelectMask();
            enableWidgets(true);
        }
        Log.i(TAG, "INFO. onResume()");
    }

    @Override
    protected void onPause() {
        if (mReader != null) {
            mReader.removeEventListener(null);
            mReader = null;
        }
        super.onPause();
        Log.i(TAG, "INFO. onPause()");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i(TAG, "INFO. onBackPressed()");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_mask_add:
                actionMaskAdd();
                break;
            case R.id.action_mask_remove:
                actionMaskRemove();
                break;
            case R.id.action_mask_clear:
                actionMaskClear();
                break;
            case R.id.inventory_option_session:
                showSessionDialog();
                break;
            case R.id.inventory_option_target:
                showTargetDialog();
                break;
            case R.id.inventory_option_select_flag:
                showSelectFlagDialog();
                break;
            case R.id.action_save:
                actionSave();
                break;
            case R.id.action_cancel:
                actionCancel();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SelectionCriterias.Criteria criteria = adpMask.getItem(position);
        SelectionMaskDialog.show(this, R.string.title_edit_select_mask, criteria,
                new SelectionMaskDialog.OnSaveSelectionMaskListener() {
                    @Override
                    public void onSaveSelectionMask(SelectionCriterias.Criteria criteria) {
                        adpMask.updateItem(position, criteria);
                    }
                });
    }

    @Override
    public void onCheckedChanged(int position, boolean isChecked) {
        lstMask.setItemChecked(position, isChecked);
        enableWidgets(true);
    }

    @Override
    public void onReaderDeviceStateChanged(DeviceEvent status) {
        switch (status) {
            case CONNECTED:
                break;
            case DISCONNECTED:
                break;
        }
    }

    @Override
    public void onReaderEvent(int event, int result, String data) {

    }

    private void actionMaskAdd() {
        SelectionCriterias.Criteria criteria = new SelectionCriterias.Criteria(
                SelectionCriterias.Target.SELECTED,
                SelectionCriterias.Bank.EPC,
                "",
                16,
                0,
                SelectionCriterias.Action.ASLINVA_DSLINVB);
        SelectionMaskDialog.show(this, R.string.title_add_select_mask, criteria,
                new SelectionMaskDialog.OnSaveSelectionMaskListener() {
                    @Override
                    public void onSaveSelectionMask(SelectionCriterias.Criteria criteria) {
                        adpMask.addItem(criteria);
                        txtEntryCount.setText(String.format(Locale.US, "%d %s",
                                adpMask.getCount(), getString(R.string.select_mask_entry_unit)));
                        enableWidgets(true);
                    }
                });
        Log.i(TAG, "INFO. actionMaskAdd()");
    }

    private void actionMaskRemove() {
        if (lstMask.getCheckedItemCount() <= 0)
            return;

        final SparseBooleanArray checkedPositionArray = lstMask.getCheckedItemPositions();
        if (checkedPositionArray == null || checkedPositionArray.size() <= 0)
            return;

        MsgBox.showQuestion(this, R.string.msg_delete_selection_mask,
                new MsgBox.OnClickListener() {
                    @Override
                    public void onOkClicked() {
                        int[] checkedPositions = new int[checkedPositionArray.size()];
                        for (int i = 0; i < checkedPositionArray.size(); i++) {
                            checkedPositions[i] = checkedPositionArray.keyAt(i);
                        }
                        adpMask.remoteItem(checkedPositions);
                        txtEntryCount.setText(String.format(Locale.US, "%d %s",
                                adpMask.getCount(), getString(R.string.select_mask_entry_unit)));
                        enableWidgets(true);
                    }
                });

        Log.i(TAG, "INFO. actionMaskRemove()");
    }

    private void actionMaskClear() {
        if (adpMask.getCount() <= 0)
            return;
        MsgBox.showQuestion(this, R.string.msg_delete_all_selection_mask,
                new MsgBox.OnClickListener() {
                    @Override
                    public void onOkClicked() {
                        adpMask.clear();
                        txtEntryCount.setText(String.format(Locale.US, "%d %s",
                                adpMask.getCount(), getString(R.string.select_mask_entry_unit)));
                        enableWidgets(true);
                    }
                });
        Log.i(TAG, "INFO. actionMaskClear()");
    }

    private void showSessionDialog() {
        SingleNameValueListDialog.show(this,
                R.string.inventory_option_session,
                session, sessionNames,
                new int[]{
                        RFID.Session.SESSION_S0,
                        RFID.Session.SESSION_S1,
                        RFID.Session.SESSION_S2,
                        RFID.Session.SESSION_S3,
                },
                new SingleNameValueListDialog.OnSelectValueListener() {
                    @Override
                    public void onSelectValue(int value) {
                        session = value;
                        txtSession.setText(sessionNames[session]);
                    }
                });
        Log.i(TAG, "INFO. showSessionDialog()");
    }

    private void showTargetDialog() {
        SingleNameValueListDialog.show(this,
                R.string.inventory_option_target,
                target, targetNames,
                new int[]{
                        RFID.InvSessionTarget.TARGET_A,
                        RFID.InvSessionTarget.TARGET_B,
                },
                new SingleNameValueListDialog.OnSelectValueListener() {
                    @Override
                    public void onSelectValue(int value) {
                        target = value;
                        txtTarget.setText(targetNames[target]);
                    }
                });
        Log.i(TAG, "INFO. showTargetDialog()");
    }

    private void showSelectFlagDialog() {
        SingleNameValueListDialog.show(this,
                R.string.inventory_option_select_flag,
                selectFlag, selectFlagNames,
                new int[]{
                        RFID.InvSelectionTarget.ALL,
                        RFID.InvSelectionTarget.UNSELECTED,
                        RFID.InvSelectionTarget.SELECTED,
                },
                new SingleNameValueListDialog.OnSelectValueListener() {
                    @Override
                    public void onSelectValue(int value) {
                        selectFlag = value;
                        txtSelectFlag.setText(selectFlagNames[selectFlag]);
                    }
                });
        Log.i(TAG, "INFO. showSelectFlagDialog()");
    }

    private void actionSave() {
        applySelectMask();
        setResult(RESULT_OK);
        finish();
        Log.i(TAG, "INFO. actionSave()");
    }

    private void actionCancel() {
        finish();
        Log.i(TAG, "INFO. actionCancel()");
    }

    private void updateSelectMask() {
        if (mReader == null)
            return;
        int value = mReader.getSelectionMaskState();
        chkUseSelectMask.setChecked(value == RFID.ON);
        SelectionCriterias criterias = mReader.getSelectionMask();
        if (criterias != null) {
            adpMask.addAllItems(criterias);
        }
        txtEntryCount.setText(String.format(Locale.US, "%d %s",
                adpMask.getCount(), getString(R.string.select_mask_entry_unit)));

        session = mReader.getSession();
        target = mReader.getInventorySessionTarget();
        selectFlag = mReader.getInventorySelectionTarget();

        txtSession.setText(sessionNames[session]);
        txtTarget.setText(targetNames[target]);
        txtSelectFlag.setText(selectFlagNames[selectFlag]);

        Log.i(TAG, "INFO. updateSelectMask()");
    }

    private void applySelectMask() {
        if (mReader == null)
            return;
        int result = RfidResult.SUCCESS;
        boolean usedSelectMask = chkUseSelectMask.isChecked();

        if ((result = mReader.setSelectionMaskState(usedSelectMask ?
                RFID.ON : RFID.OFF)) != RfidResult.SUCCESS) {
            MsgBox.showError(this, R.string.msg_fail_set_selection_mask_state);
            return;
        }
        SelectionCriterias criterias = adpMask.getSelectionCrierias();
        if (criterias.getCriteria().size() > 0) {
            if ((result = mReader.setSelectionMask(criterias)) != RfidResult.SUCCESS) {
                MsgBox.showError(this, R.string.msg_fail_set_selection_mask);
                return;
            }
        } else {
            if ((result = mReader.removeSelectionMask()) != RfidResult.SUCCESS) {
                MsgBox.showError(this, R.string.msg_fail_remove_selection_mask);
                return;
            }
        }
        if ((result = mReader.setSession(session)) != RfidResult.SUCCESS) {
            MsgBox.showError(this, R.string.msg_fail_set_session);
            return;
        }
        if ((result = mReader.setInventorySessionTarget(target)) != RfidResult.SUCCESS) {
            MsgBox.showError(this, R.string.msg_fail_set_target);
            return;
        }
        if ((mReader.setInventorySelectionTarget(selectFlag)) != RfidResult.SUCCESS) {
            MsgBox.showError(this, R.string.msg_fail_set_select_target);
            return;
        }

        Log.i(TAG, "INFO. applySelectMask()");
    }

    private void enableWidgets(boolean enabled) {
        boolean connected = mReader != null && mReader.isConnected();

        if (adpMask.getCount() <= 0)
            chkUseSelectMask.setChecked(false);
        chkUseSelectMask.setEnabled(enabled && connected && adpMask.getCount() > 0);
        txtEntryCount.setEnabled(enabled && connected);
        lstMask.setEnabled(enabled && connected);
        btnMaskAdd.setEnabled(enabled && connected);
        btnMaskRemove.setEnabled(enabled && connected && lstMask.getCheckedItemCount() > 0);
        btnMaskClear.setEnabled(enabled && connected && adpMask.getCount() > 0);
        txtSession.setEnabled(enabled && connected);
        txtTarget.setEnabled(enabled && connected);
        txtSelectFlag.setEnabled(enabled && connected);
        btnSave.setEnabled(enabled && connected);
    }
}