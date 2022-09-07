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
 * File: MainActivity.java
 * Date: 2022.05.31
 * Author: HyungChan Bae, chan941027@apulsetech.com
 *
 *
 * Modify : 2022.06.09. ncsin4  Add selection mask activity
 *
 ****************************************************************************
 */

package com.apulsetech.sample.bluetooth.rfid.inventory_sample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.apulsetech.lib.event.DeviceEvent;
import com.apulsetech.lib.event.ReaderEventListener;
import com.apulsetech.lib.remote.type.RemoteDevice;
import com.apulsetech.lib.rfid.Reader;
import com.apulsetech.lib.rfid.type.RFID;
import com.apulsetech.lib.rfid.type.RfidResult;
import com.apulsetech.lib.rfid.type.SelectionCriterias;
import com.apulsetech.lib.util.LogUtil;
import com.apulsetech.sample.bluetooth.rfid.inventory_sample.adapters.TagListAdapter;
import com.apulsetech.sample.bluetooth.rfid.inventory_sample.data.Const;
import com.apulsetech.sample.bluetooth.rfid.inventory_sample.dialogs.MsgBox;
import com.apulsetech.sample.bluetooth.rfid.inventory_sample.dialogs.WaitDialog;
import com.apulsetech.sample.bluetooth.rfid.inventory_sample.dialogs.PowerGainDialog;
import com.apulsetech.sample.bluetooth.rfid.inventory_sample.utlities.AppInfoUtil;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ReaderEventListener,
        View.OnClickListener {
    private static final String TAG = "MainActivity";

    private static final boolean D = true;

    private static final int TIMEOUT = 30000;

    private static final String PREF_NAME = "rfid_inventory_sample";
    private static final String LAST_ADDRESS = "last_dev_address";

    private TextView txtVersion;
    private TextView txtConnState;
    private ListView lstTags;
    private TagListAdapter adpTags;
    private TextView txtPowerLevel;
    private TextView txtMaskCount;
    private TextView txtMaskState;
    private TextView txtMaskSession;
    private TextView txtMaskTarget;
    private TextView txtMaskSelect;
    private TextView txtAllCount;
    private TextView txtCount;
    private Button btnInventory;
    private Button btnClear;
    private Button btnMask;
    private CheckBox mHoldTriggerCheckBox;

    private boolean mInventoryStarted = false;
    private boolean mHoldTriggerEnabled = true;
    private boolean mOperationSettingsExpanded = false;

    private int ret;

    private BluetoothAdapter btAdapter;

    private MenuItem mnuDiscoveryDevice = null;
    private MenuItem mnuReconnect = null;
    private MenuItem mnuDisconnect = null;

    private RemoteDevice mLastDevice = null;
    private Reader mReader = null;

    private String[] sessionNames;
    private String[] targetNames;
    private String[] selectFlagNames;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setIcon(R.mipmap.ic_app);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        txtVersion = findViewById(R.id.version);
        txtVersion.setText(AppInfoUtil.getVersion(this));
        txtConnState = findViewById(R.id.connect_state);
        txtConnState.setText(R.string.connection_state_disconnected);

        lstTags = findViewById(R.id.inventory_list);
        adpTags = new TagListAdapter();
        lstTags.setAdapter(adpTags);
        lstTags.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        txtPowerLevel = findViewById(R.id.rf_power);
        txtPowerLevel.setText("30 dBm");
        txtPowerLevel.setOnClickListener(this);

        txtMaskCount = findViewById(R.id.selection_mask_count);
        txtMaskState = findViewById(R.id.selection_mask_state);
        txtMaskSession = findViewById(R.id.selection_mask_session);
        txtMaskTarget = findViewById(R.id.selection_mask_target);
        txtMaskSelect = findViewById(R.id.selection_mask_selection_flag);

        txtAllCount = findViewById(R.id.all_count);
        txtCount = findViewById(R.id.overlap_count);

        btnInventory = findViewById(R.id.action_inventory);
        btnInventory.setOnClickListener(this);

        btnClear = findViewById(R.id.action_clear);
        btnClear.setOnClickListener(this);

        btnMask = findViewById(R.id.action_mask);
        btnMask.setOnClickListener(this);

        mHoldTriggerCheckBox = findViewById(R.id.buttonTrigger);

        sessionNames = getResources().getStringArray(R.array.session);
        targetNames = getResources().getStringArray(R.array.session_target);
        selectFlagNames = getResources().getStringArray(R.array.selection_flag_simple);

        txtPowerLevel.setText("30 dBm");
        txtMaskCount.setText("0");
        txtMaskState.setText(getString(R.string.select_mask_disabled));
        txtMaskSession.setText(sessionNames[0]);
        txtMaskTarget.setText(targetNames[0]);
        txtMaskSelect.setText(selectFlagNames[0]);

        mHoldTriggerCheckBox.setChecked(mHoldTriggerEnabled);

        mHoldTriggerCheckBox.setOnCheckedChangeListener(mCheckChangeListener);

        initBluetooth();
        loadConfig();

        updateCount();

        enableWidgets(true);

        Log.i(TAG, "INFO. onCreate()");
    }

    @Override
    protected void onResume() {
        super.onResume();

        mReader = Reader.getReader(this);
        if (mReader != null) {
            mReader.setEventListener(this);
            if (mReader.isConnected()) {
                updateReaderInfo();
            }
        }
        enableWidgets(true);
        Log.i(TAG, "INFO. onResume()");
    }

    @Override
    protected void onStart() {
        super.onStart();

        enableWidgets(true);

        Log.i(TAG, "INFO. onStart()");
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
    protected void onDestroy() {
        if (mReader != null) {
            if (mReader.isOperationRunning()) {
                mReader.stopOperation();
            }
            mReader.stop();
            mReader.destroy();
            mReader = null;
            txtConnState.setText(R.string.connection_state_disconnected);
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mReader != null) {
            if (mReader.isOperationRunning()) {
                mReader.stopOperation();
            }
            mReader.setEventListener(null);
            mReader.stop();
            mReader.destroy();
            mReader = null;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_option, menu);
        mnuDiscoveryDevice = menu.findItem(R.id.discovery_device);
        mnuReconnect = menu.findItem(R.id.reconnect);
        mnuDisconnect = menu.findItem(R.id.disconnect);
        enableWidgets(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
           case R.id.discovery_device:
               showDiscoveryDevice();
                break;
            case R.id.disconnect:
                actionDisconnect();
                break;
            case R.id.reconnect:
                actionConnect();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rf_power:
                showPowerLevelDialog();
                break;
            case R.id.action_inventory:
                inventory();
                break;
            case R.id.action_clear:
                adpTags.clear();
                updateCount();
                break;
            case R.id.action_mask:
                showSelectMaskDialog();
                break;
        }
    }

    @Override
    public void onReaderDeviceStateChanged(DeviceEvent status) {
        switch (status) {
            case CONNECTED:
                txtConnState.setText(R.string.connection_state_connected);
                break;
            case DISCONNECTED:
                txtConnState.setText(R.string.connection_state_disconnected);
                break;
        }
    }

    @Override
    public void onReaderEvent(int event, int result, String data) {
        switch (event) {
            case Reader.READER_CALLBACK_EVENT_INVENTORY:
                addTagData(data);
                Log.d(TAG, String.format(Locale.US,
                        "EVENT. INVENTORY [%s]", data));
                break;
        }
    }

    private void initBluetooth() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter.isEnabled()) {
            enableWidgets(true);
        } else {
            showRequestEnableBluetooth();
        }
        Log.i(TAG, "INFO. initBluetooth()");
    }

    private void showRequestEnableBluetooth() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        enableWidgets(true);
                    } else {
                        MsgBox.show(MainActivity.this,
                                R.string.msg_enable_bluetooth,
                                new MsgBox.OnClickListener() {
                                    @Override
                                    public void onOkClicked() {
                                        enableWidgets(true);
                                    }
                                });
                    }
                });
        launcher.launch(intent);
        Log.i(TAG, "INFO. showRequestEnableBluetooth()");
    }

    private ActivityResultLauncher<Intent> launcherDiscoveringResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if ((mLastDevice = result.getData()
                            .getParcelableExtra(Const.REMOTE_DEVICE)) != null) {
                        saveConfig();
                        if (mReader == null) {
                            mReader = Reader.getReader(MainActivity.this);
                        }
                        if (mReader != null) {
                            if (mReader.isConnected()) {
                                updateReaderInfo();
                            }
                        }
                    }
                } else {
                    if (mReader != null) {
                        if (mReader.isConnected()) {
                            if (mReader.isOperationRunning()) {
                                mReader.stopOperation();
                            }
                            mReader.stop();
                        }
                        mReader = null;
                    }
                }
                enableWidgets(true);
            });

    private final CompoundButton.OnCheckedChangeListener mCheckChangeListener =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int id = buttonView.getId();
                    if (id == R.id.buttonTrigger) {
                        mHoldTriggerEnabled = isChecked;
                    }
                }
            };


    private void showDiscoveryDevice() {
        Intent intent = new Intent(this, DiscoveryDeviceActivity.class);
        launcherDiscoveringResult.launch(intent);
        Log.i(TAG, "INFO. showDiscoveryDevice()");
    }

    private void actionConnect() {
        WaitDialog.show(this, getString(R.string.msg_connect_device));
        mReader = Reader.getReader(this,
                mLastDevice, false, TIMEOUT);
        if (mReader != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mReader.start()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                WaitDialog.hide();
                                mReader.setEventListener(MainActivity.this);
                                if (mReader.isConnected()) {
                                    updateReaderInfo();
                                }
                                enableWidgets(true);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                WaitDialog.hide();
                                mReader = null;
                                enableWidgets(true);
                                MsgBox.show(MainActivity.this,
                                        R.string.msg_fail_connect);

                            }
                        });
                    }
                }
            }).start();
        } else {
            WaitDialog.hide();
            mReader = null;
            enableWidgets(true);
            MsgBox.show(MainActivity.this,
                    R.string.msg_fail_connect);
        }
        Log.i(TAG, "INFO. actionConnect()");
    }

    private void actionDisconnect() {
        if (mReader != null) {
            WaitDialog.show(this, getString(R.string.msg_disconnect_device));
            if (mReader.isOperationRunning()) {
                mReader.stopOperation();
            }
            mReader.stop();
            mReader.destroy();
            mReader = null;
            enableWidgets(true);
            WaitDialog.hide();
        } else if (mReader == null) {
            MsgBox.show(MainActivity.this,
                    R.string.msg_disconnect_error);
        }
        Log.i(TAG, "INFO. actionDisconnect()");
    }

    private void inventory() {
        if (mReader == null) {
            Log.e(TAG, String.format(Locale.US,
                    "ERROR. inventory() - Failed to invalid reader object"));
            return;
        }
        if (mReader.isOperationRunning()) {
            ret =  mReader.stopOperation();
            btnInventory.setText(R.string.action_inventory);
        } else {
            ret = mReader.startInventory();
            btnInventory.setText(R.string.action_stop);
        }
        enableWidgets(true);
        Log.i(TAG, String.format(Locale.US, "INFO. inventory()"));
    }

    private void showPowerLevelDialog() {
        if (mReader == null)
            return;
        if (!mReader.isConnected())
            return;

        int powerGain = mReader.getRadioPower();
        PowerGainDialog.show(this,
                RFID.Power.MIN_POWER, RFID.Power.MAX_POWER, powerGain,
                new PowerGainDialog.OnSelectPowerGainListener() {
                    @Override
                    public void onSelectPowerGain(int powerGain) {
                        mReader.setRadioPower(powerGain);
                        txtPowerLevel.setText(String.format(Locale.US, "%d dBm",
                                powerGain));
                    }
                });

        Log.i(TAG, "INFO. showPowerLevelDialog()");
    }

    private void showSelectMaskDialog() {
        if (mReader == null)
            return;
        if (!mReader.isConnected())
            return;

        Intent intent = new Intent(this, SelectMaskActivity.class);
        startActivity(intent);

        Log.i(TAG, "INFO. showSelectMaskDialog()");
    }

    private void addTagData(String data) {
        int pos = data.indexOf(";");
        if (pos >= 0) {
            String tag = data.substring(0, pos - 1);
            adpTags.add(tag);
            updateCount();
        }
    }

    private void enableWidgets(boolean enabled) {
        boolean isBluetooth = false;
        boolean isDisconnected = false;
        boolean isConnected = false;
        boolean isOperation = false;
        if (btAdapter != null)
            isBluetooth = btAdapter.isEnabled();
        isDisconnected = mReader == null;
        if (mReader != null) {
            isConnected = mReader.isConnected();
            isOperation = mReader.isOperationRunning();
        }

        if (mnuDiscoveryDevice != null)
            mnuDiscoveryDevice.setVisible(enabled && isBluetooth && isDisconnected);
        if (mnuReconnect != null)
            mnuReconnect.setVisible(enabled && isBluetooth && isDisconnected && mLastDevice != null);
        if (mnuDisconnect != null)
            mnuDisconnect.setVisible(enabled && isBluetooth && !isDisconnected && isConnected);
        txtPowerLevel.setEnabled(enabled && isBluetooth && !isDisconnected && isConnected);
        btnInventory.setEnabled(enabled && isBluetooth && !isDisconnected && isConnected);
        btnClear.setEnabled(enabled && isBluetooth && !isDisconnected && isConnected && !isOperation);
        btnMask.setEnabled(enabled && isBluetooth && !isDisconnected && isConnected && !isOperation);
        mHoldTriggerCheckBox.setEnabled(enabled && isBluetooth && !isDisconnected && isConnected && !isOperation);
        txtConnState.setText(isDisconnected ?
                R.string.connection_state_disconnected :
                (isConnected ?
                        R.string.connection_state_connected :
                        R.string.connection_state_connecting));
    }

    private void updateCount() {
        txtAllCount.setText(String.format(Locale.US, "%d", adpTags.getTotalCount()));
        txtCount.setText(String.format(Locale.US, "%d", adpTags.getCount()));
        Log.i(TAG, "INFO. updateCount()");
    }

    private void updateReaderInfo() {
        int powerGain = mReader.getRadioPower();
        int maskState = mReader.getSelectionMaskState();
        SelectionCriterias criterias = mReader.getSelectionMask();
        int session = mReader.getSession();
        int target = mReader.getInventorySessionTarget();
        int selectFlag = mReader.getInventorySelectionTarget();

        txtPowerLevel.setText(String.format(Locale.US, "%d dBm",
                powerGain));
        txtMaskCount.setText(String.format(Locale.US,
                "%d", criterias.getCriteria().size()));
        txtMaskState.setText(getString(maskState == RFID.ON ?
                        R.string.select_mask_enabled : R.string.select_mask_disabled));
        txtMaskSession.setText(sessionNames[session]);
        txtMaskTarget.setText(targetNames[target]);
        txtMaskSelect.setText(selectFlagNames[selectFlag]);
        Log.i(TAG, "INFO. updateReaderInfo()");
    }

    // Load Configuration
    private void loadConfig() {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String address = pref.getString(LAST_ADDRESS, "");
        if (address != null && address.length() > 0) {
            BluetoothDevice btDevice = btAdapter.getRemoteDevice(address);
            if (btDevice != null) {
                mLastDevice = RemoteDevice.makeBtSppDevice(btDevice);
            }
        }
        Log.i(TAG, "INFO. loadConfig()");
    }

    // Save Configuration
    private void saveConfig() {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor writer = pref.edit();
        String addrss;
        if (mLastDevice == null) {
            addrss = "";
        } else {
            addrss = mLastDevice.getAddress();
        }
        writer.putString(LAST_ADDRESS, addrss);
        writer.commit();
        Log.i(TAG, "INFO. saveConfig()");
    }

    private void processKeyDown() {
        LogUtil.log(LogUtil.LV_D, D, TAG, "processKeyDown()");

        if (!mInventoryStarted) {
             inventory();
            if (ret == RfidResult.SUCCESS) {
                mInventoryStarted = true;
                enableWidgets(false);
            }else if (ret == RfidResult.LOW_BATTERY)
            {
                Toast.makeText(this, "Low battery!",
                        Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this,
                        "Failed to start inventory!", Toast.LENGTH_SHORT).show();
            }
        } else {
            inventory();
            if (ret == RfidResult.SUCCESS) {
                mInventoryStarted = false;
                enableWidgets(true);
            } else if (ret == RfidResult.STOP_FAILED_TRY_AGAIN) {
                Toast.makeText(this,
                        "Failed to stop inventory!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processKeyUp() {
        LogUtil.log(LogUtil.LV_D, D ,TAG, "processKeyUp()");

        if (mInventoryStarted && !mHoldTriggerEnabled) {
            inventory();
            if (ret == RfidResult.SUCCESS) {
                mInventoryStarted = false;
                enableWidgets(true);
            } else if (ret == RfidResult.STOP_FAILED_TRY_AGAIN) {
                Toast.makeText(this,
                        "Failed to stop inventory!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Rfid Reader Key process
    @Override
    public void onReaderRemoteKeyEvent(int action, int keyCode) {
        LogUtil.log(LogUtil.LV_D, D ,TAG, "onReaderRemoteKeyEvent : action=" + action + "keyCode" + keyCode);

        if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            if (action == KeyEvent.ACTION_DOWN) {
                processKeyDown();
            }else if (action == KeyEvent.ACTION_UP) {
                processKeyUp();
            }
        }
    }
}



