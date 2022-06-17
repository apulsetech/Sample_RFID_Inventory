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
 * File: DiscoveryDeviceActivity.java
 * Date: 2022.05.31
 * Author: HyungChan Bae, chan941027@apulsetech.com
 *
 ****************************************************************************
 */

package com.example.rfid_inventory_sample;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apulsetech.lib.remote.type.RemoteDevice;
import com.apulsetech.lib.rfid.Reader;
import com.example.rfid_inventory_sample.adapters.DeviceListAdapter;
import com.example.rfid_inventory_sample.data.Const;
import com.example.rfid_inventory_sample.dialogs.MsgBox;
import com.example.rfid_inventory_sample.dialogs.WaitDialog;

import java.util.Locale;
import java.util.Set;

public class DiscoveryDeviceActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = DiscoveryDeviceActivity.class.getSimpleName();

    private static final int TIMEOUT = 30000;

    private ListView lstPairedDevices;
    private DeviceListAdapter adpPairedDevices;
    private ProgressBar pgbDiscoveringDevices;
    private ListView lstDiscoveringDevices;
    private DeviceListAdapter adpDiscoveringDevices;
    private Button btnAction;

    private BluetoothAdapter btAdapter;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery_device);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btAdapter = BluetoothAdapter.getDefaultAdapter();//BluetoothAdapter 객체를 획득한다.

        lstPairedDevices = findViewById(R.id.paired_devices);
        adpPairedDevices = new DeviceListAdapter();
        lstPairedDevices.setAdapter(adpPairedDevices);
        lstPairedDevices.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lstPairedDevices.setOnItemClickListener(this);

        pgbDiscoveringDevices = findViewById(R.id.discovering_progress);
        pgbDiscoveringDevices.setVisibility(View.GONE);

        lstDiscoveringDevices = findViewById(R.id.discovering_devices);
        adpDiscoveringDevices = new DeviceListAdapter();
        lstDiscoveringDevices.setAdapter(adpDiscoveringDevices);
        lstDiscoveringDevices.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lstDiscoveringDevices.setOnItemClickListener(this);

        btnAction = findViewById(R.id.action_discovering);
        btnAction.setOnClickListener(this);

        fillPairedDevices();

        Log.i(TAG, "INFO. onCreate()");
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (btAdapter.isDiscovering()) {
                    btAdapter.cancelDiscovery();
                }
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBackPressed() {
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }
        super.onBackPressed();
        Log.i(TAG, "INFO. onBackPressed()");
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_discovering:
                btnAction.setEnabled(false);
                if (btAdapter.isDiscovering()) {
                    stopDiscovering();
                } else {
                    startDiscovering();
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RemoteDevice device = null;
        switch (parent.getId()) {
            case R.id.paired_devices:
                device = adpPairedDevices.getItem(position);
                break;
            case R.id.discovering_devices:
                device = adpDiscoveringDevices.getItem(position);
                break;
            default:
                return;
        }
        WaitDialog.show(this, getString(R.string.msg_connect_device));
        Reader reader = Reader.getReader(DiscoveryDeviceActivity.this,
                device, false, TIMEOUT);
        if (reader != null) {
            final RemoteDevice finalDevice = device;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (reader.start()) {
                        // Connected device
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                WaitDialog.hide();
                                Intent intent = new Intent();
                                intent.putExtra(Const.REMOTE_DEVICE, finalDevice);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        });
                    } else {
                        // Failed to start reader service
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                WaitDialog.hide();
                                MsgBox.show(DiscoveryDeviceActivity.this,
                                        R.string.msg_fail_connect);
                            }
                        });
                    }
                }
            }).start();
        } else {
            WaitDialog.hide();
            MsgBox.show(DiscoveryDeviceActivity.this,
                    R.string.msg_fail_connect);
        }
    }

    @SuppressLint("MissingPermission")
    private void fillPairedDevices() {
        adpPairedDevices.clear();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String name = device.getName();
                String address = device.getAddress();
                if (name != null && name.length() > 2) {
                    if (name.substring(0, 1).equals("α")) {
                        adpPairedDevices.add(RemoteDevice.makeBtSppDevice(device));
                        adpPairedDevices.notifyDataSetChanged();
                        Log.d(TAG, String.format(Locale.US,
                                "DEBUG. PAIRED DEVICE [[%s], [%s]]",
                                name, address));
                    }
                }
            }
            adpPairedDevices.notifyDataSetChanged();
        }
        Log.i(TAG, "INFO. fillPairedDevices()");
    }

    @SuppressLint("MissingPermission")
    private void startDiscovering() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        registerReceiver(receiver, filter);

        adpDiscoveringDevices.clear();
        btAdapter.startDiscovery();
    }


    @SuppressLint("MissingPermission")
    private void stopDiscovering() {
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }
    }

    @SuppressLint("MissingPermission")
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                String address = device.getAddress(); // MAC address
                if (name != null && name.length() > 2) {
                    if (name.substring(0, 1).equals("α")) {
                        adpDiscoveringDevices.add(RemoteDevice.makeBtSppDevice(device));
                        adpDiscoveringDevices.notifyDataSetChanged();
                    }
                }
                Log.d(TAG, String.format(Locale.US, "DEBUG. ACTION_FOUND [[%s], [%s]]", name, address));

            } else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                String address = device.getAddress(); // MAC address
                if (name != null && name.length() > 2) {
                    if (name.substring(0, 1).equals("α")) {
                        adpDiscoveringDevices.add(RemoteDevice.makeBtSppDevice(device));
                        adpDiscoveringDevices.notifyDataSetChanged();
                    }
                }
                Log.d(TAG, String.format(Locale.US, "DEBUG. ACTION_FOUND [[%s], [%s]]", name, address));
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                pgbDiscoveringDevices.setVisibility(View.VISIBLE);
                btnAction.setText(R.string.action_stop_discovering);
                btnAction.setEnabled(true);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                pgbDiscoveringDevices.setVisibility(View.GONE);
                btnAction.setText(R.string.action_start_discovering);
                btnAction.setEnabled(true);
                unregisterReceiver(receiver);
            }
        }
    };
}