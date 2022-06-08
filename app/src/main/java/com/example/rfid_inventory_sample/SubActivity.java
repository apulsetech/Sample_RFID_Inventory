package com.example.rfid_inventory_sample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.apulsetech.lib.remote.type.RemoteDevice;
import com.apulsetech.lib.rfid.Reader;
import com.example.rfid_inventory_sample.adapters.DeviceListAdapter;
import com.example.rfid_inventory_sample.data.Const;
import com.example.rfid_inventory_sample.dialogs.MsgBox;

import java.util.Locale;
import java.util.Set;

public class SubActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener {


    private static final String TAG = SubActivity.class.getSimpleName();

    private static final int TIMEOUT = 30000;

    private Button btnAction;
    private ListView lstPairedDevices;
    private ListView lstPairingDevices;

    private BluetoothAdapter btAdapter;
    private DeviceListAdapter adpPairedDevices;
    private DeviceListAdapter adpPairingDevices;

    private Handler mHandler;

    private ProgressDialog dialog;
    private CheckTypeTask task;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discovery_device);

        btAdapter = BluetoothAdapter.getDefaultAdapter();//BluetoothAdapter 객체를 획득한다.

        btnAction = (Button) findViewById(R.id.action_discovering);
        btnAction.setOnClickListener(this);
        lstPairedDevices = (ListView) findViewById(R.id.paired_device);
        lstPairingDevices = (ListView) findViewById(R.id.discovering_devices);

//        btAddress = new ArrayList<>();

        // Show paired devices
        adpPairedDevices = new DeviceListAdapter();
        lstPairedDevices.setAdapter(adpPairedDevices);

        adpPairingDevices = new DeviceListAdapter();
        lstPairingDevices.setAdapter(adpPairingDevices);

        lstPairedDevices.setOnItemClickListener(this);

        lstPairingDevices.setOnItemClickListener(this);

        mHandler = new Handler();

        dialog = new ProgressDialog(this);

        loadPairedDevices();

    }

    @SuppressLint("MissingPermission")
    private void loadPairedDevices() {
        adpPairedDevices.clear();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        //getBondedDevices() = 페어링 된 블루투스 디바이스를 넘겨받음
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String name = device.getName();
                String address = device.getAddress(); // MAC address

                if(name != null && name.length() > 2){
                    if(name.substring(0,1).equals("α")){
                        adpPairedDevices.add(device);

                    }
                }

//                adpPairedDevices.add(device);
//                btAddress.add(address);
            }
            adpPairedDevices.notifyDataSetChanged();
            //notifyDataSetChanged() 호출하여 새로 고침
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBackPressed() {
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }
        super.onBackPressed();
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

    @SuppressLint("MissingPermission")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }

//        WaitDialog.show(this);

        RemoteDevice device = null;
        switch (parent.getId()) {
            case R.id.paired_device:
//                task.execute();
                device = adpPairedDevices.getItem(position);
                Log.d(TAG, String.format(Locale.US, "DEBUG. onItemClick(%d) - select paired device [%s:%s]",
                        position, device.getName(), device.getAddress()));
                break;

            case R.id.discovering_devices:
//                task.execute();
                device = adpPairingDevices.getItem(position);
                Log.d(TAG, String.format(Locale.US, "DEBUG. onItemClick(%d) - select pairing device [%s:%s]",
                        position, device.getName(), device.getAddress()));
                break;
        }

        Reader reader = Reader.getReader(this,
                device, TIMEOUT);

        Log.d(TAG, String.format(Locale.US, "INFO. onItemClick() - create reader [%s:%s]",
                device.getName(), device.getAddress()));

        if (reader != null) {
            task = new CheckTypeTask();
            task.execute(reader);
        } else {
            MsgBox.show(SubActivity.this, R.string.msg_fail_connect);
        }
    }

    private class CheckTypeTask extends AsyncTask<Reader, Void, RemoteDevice>{

//        CustomDialog waitDialog = new CustomDialog(SubActivity.this);

        @Override
        protected void onPreExecute(){

            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Loading...");

            dialog.show();
        }

        @Override
        protected RemoteDevice doInBackground(Reader params[]) {
            if (params == null || params.length < 0) {
                return null;
            }
            Reader reader = params[0];
            RemoteDevice device = reader.getRemoteDevice();

            if (!reader.start()) {
                //WaitDialog.hide();
                // 장비와 접속이 실패하였습니다. 확인하여 주십시요.
                Log.e(TAG, String.format(Locale.US, "ERROR. onItemClick() - Failed to start service at remote device [%s:%s]",
                        device.getName(), device.getAddress()));
                return null;
            }
            Log.i(TAG, String.format(Locale.US, "INFO. onItemClick() - Start remote device [%s:%s]",
                    device.getName(), device.getAddress()));

            return device;
        }

        @Override
        public void onPostExecute(RemoteDevice result){
            dialog.dismiss();
            if (result == null) {
                MsgBox.show(SubActivity.this, R.string.msg_fail_connect);
            } else {
                Intent intent = new Intent();
                intent.putExtra(Const.REMOTE_DEVICE, result);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    }


    @SuppressLint("MissingPermission")
    private void startDiscovering() {
        IntentFilter filter;
        /* IntentFilter 암시적 인텐트(인텐트의 액션, 데이터를 지정했지만 호출할 대상이 달라질 수 있는 경우)를 통해 사용자로
          하여금 어느 앱을 사용할 지 선택하도록 하고자 할 때 사용*/

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //ACTION_DISCOVERY_STARTED 브로드캐스트 작업 : 로컬 블루투스 어댑터가 원격 장치 검색 프로세스를 시작
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //ACTION_DISCOVERY_FINISHED 브로드캐스트 작업 : 로컬 블루투스 어댑터가 장치검색을 완료했습니다.
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_NAME_CHANGED);
        registerReceiver(receiver, filter);

        adpPairingDevices.clear();
        btAdapter.startDiscovery();
    }


    @SuppressLint("MissingPermission")
    private void stopDiscovering() {
        if (btAdapter.isDiscovering()) {//isDiscovering() 기기가 검색 수행중인지 확인
            btAdapter.cancelDiscovery();//블루투스 중지
        }
    }

    @SuppressLint("MissingPermission")
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //getParcelableExtra() putExtra로 넘긴 Parcelable 받아올 때 사용
                String name = device.getName();
                String address = device.getAddress(); // MAC address

                if(name !=null && name.length() > 2){
                    if(name.substring(0,1).equals("α")){
                        adpPairingDevices.add(device);

                    }
                }
                Log.d(TAG, String.format(Locale.US, "DEBUG. ACTION_FOUND [[%s], [%s]]", name, address));

                //btArrayAdapter.add(deviceName);
//                btAddress.add(address);

                adpPairingDevices.notifyDataSetChanged();
                //리스트 목록 갱신

            } else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String name = device.getName();//검색된 블루투스 기기 이름
                String address = device.getAddress(); //검색된 기기의 블루투스 주소

                if(name !=null && name.length() > 2){
                    if(name.substring(0,1).equals("α")){
                        adpPairingDevices.add(device);

                    }
                }

                Log.d(TAG, String.format(Locale.US, "DEBUG. ACTION_NAME_CHANGED [[%s], [%s]]", name, address));
                // 아답터에서 address로 아이템을 찾아서 name을 업데이트 해줘야 함...
                //adpPairingDevices.add(device);
                adpPairingDevices.notifyDataSetChanged();





            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                btnAction.setText(R.string.action_stop_discovering);//버튼 텍스트(STOP)변경
                btnAction.setEnabled(true);//button.setEnable(true) 버튼 활성화
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                btnAction.setText(R.string.action_start_discovering);
                btnAction.setEnabled(true);
                unregisterReceiver(receiver);
            }
        }
    };

//    public void showWaitDialog() {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (waitDialog != null) {
//                    waitDialog.dismiss();
//                    waitDialog = null;
//                }
//                waitDialog = new CustomDialog(SubActivity.this);
//                waitDialog.setCancelable(false);
//                waitDialog.show();
//            }
//        });
//
//    }
//
//    public void hideWaitDialog() {
//
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (waitDialog == null)
//                    return;
//                waitDialog.dismiss();
////                waitDialog.hide();
//                waitDialog = null;
//            }
//        });
//    }

}