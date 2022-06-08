package com.example.rfid_inventory_sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.apulsetech.lib.event.DeviceEvent;
import com.apulsetech.lib.event.ReaderEventListener;
import com.apulsetech.lib.remote.type.RemoteDevice;
import com.apulsetech.lib.rfid.Reader;
import com.example.rfid_inventory_sample.adapters.TagListAdapter;
import com.example.rfid_inventory_sample.data.Const;
import com.example.rfid_inventory_sample.dialogs.MsgBox;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ReaderEventListener, View.OnClickListener {
    private final String TAG = "MainActivity";

    private static final int REQUEST_PERMISSION = 1000;
    private static final int REQ_DISCOVERY_DEVICE = 1001;
    private static final int REQUEST_ENABLE_BT = 1002;

    private static final int TIMEOUT = 30000;

    private static final String[] PERMISSIONS_11 = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private static final String[] PERMISSIONS_12 = {
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE
    };

    private BluetoothAdapter btAdapter;

    private Reader mReader = null;

    private Handler mHandler;

    public TextView txtConnState;
    private ListView lstTags;
    public TextView txtAllCount;
    public TextView txtCount;
    private Button btnInventory;
    private Button btnClear;
    private Button btnMask;

    private TagListAdapter adpTags;

    private MenuItem mnuDiscoveryDevice;
    private MenuItem mnuReconnect;
    private MenuItem mnuDisconnect;

//    private CustomDialog waitDialog;
    private ProgressDialog dialog;

    private MainActivity.CheckTypeTask task;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtConnState = findViewById(R.id.connect_state);
        txtConnState.setText(R.string.connection_state_disconnected);

        lstTags = findViewById(R.id.inventory_list);
        adpTags = new TagListAdapter();
        lstTags.setAdapter(adpTags);

        txtAllCount = findViewById(R.id.all_count);
        txtCount = findViewById(R.id.overlap_count);

        btnInventory = findViewById(R.id.button_inventory);
        btnInventory.setOnClickListener(this);

        btnClear = findViewById(R.id.button_clear);
        btnClear.setOnClickListener(this);

        btnMask = findViewById(R.id.button_mask);
        btnMask.setOnClickListener(this);

        mHandler = new Handler();

//        waitDialog = new CustomDialog(this);
        dialog = new ProgressDialog(this);

        String[] target_permission = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            target_permission = PERMISSIONS_11;
        } else {
            target_permission = PERMISSIONS_12;
        }
        // 여기서 GRANT되지 않은 퍼미션을 검사
        ArrayList<String> permissions = new ArrayList<>();
        for (String permission : target_permission) {
            if (checkSelfPermission(permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                permissions.add(permission);
            }
        }
        // DENIED된 PERMISSION이 있다면 권한 요청
        if (permissions.size() > 0) {
            requestPermissions(permissions.toArray(new String[permissions.size()]),
                    REQUEST_PERMISSION);
        } else {
            initBluetooth();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mReader = Reader.getReader(this);
        if (mReader != null) {
            mReader.setEventListener(this);

            btnInventory.setEnabled(true);
            btnClear.setEnabled(true);
            btnMask.setEnabled(false);
        } else {
            btnInventory.setEnabled(false);
            btnClear.setEnabled(false);
            btnMask.setEnabled(false);
        }
    }

    @Override
    protected void onPause() {

        if (mReader != null) {
            mReader.removeEventListener(null);
            mReader = null;
        }
        super.onPause();
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
            mnuDiscoveryDevice.setVisible(true);
            mnuReconnect.setVisible(true);
            mnuDisconnect.setVisible(false);
            txtConnState.setText(R.string.connection_state_disconnected);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_option, menu);
        mnuDiscoveryDevice = menu.findItem(R.id.discovery_device);
        mnuReconnect = menu.findItem(R.id.reconnect);
        mnuDisconnect = menu.findItem(R.id.disconnect);

        RemoteDevice device = loadDevice();
        if (device == null) {
            mnuReconnect.setEnabled(false);
        } else {
            mnuReconnect.setEnabled(true);
        }
        mnuDisconnect.setVisible(false);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.discovery_device:
                Intent intent = new Intent(this, SubActivity.class);
                startActivityForResult(intent, REQ_DISCOVERY_DEVICE);
                break;

            case R.id.disconnect:
                if (mReader != null) {
                    mReader.stop();
                    //기기 연결 해제
                    btnDisconnect();
                    break;
                }else if(mReader == null){
                    //Toast.makeText(this, "비정상적인 접근 방식입니다. 앱을 재 실행 해주세요.", Toast.LENGTH_SHORT).show();
                    MsgBox.show(MainActivity.this, R.string.msg_disconnect_error);
                }
                break;

            case R.id.reconnect:

                RemoteDevice device = loadDevice();

                mReader = Reader.getReader(this,
                        device, TIMEOUT);

                if(mReader != null) {
                    task = new CheckTypeTask();
                    task.execute(mReader);
                }else {
                    MsgBox.show(MainActivity.this, R.string.msg_fail_connect);
                }
//                initViewReconnect();

        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_inventory:
                inventory();
                break;
            case R.id.button_clear:
                adpTags.clear();
                txtAllCount.setText("0");
                txtCount.setText("0");
                break;
            case R.id.button_mask:
                break;

        }
    }

    @Override
    public void onReaderDeviceStateChanged(DeviceEvent status) {
//        switch (status) {
//            case CONNECTED:
//                break;
//            case DISCONNECTED:
//                break;
//        }
//        if(status == DeviceEvent.DISCONNECTED){
//            txtConnState.setText(R.string.connection_state_disconnect);
//        }

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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        boolean anyDenied = false;
        if (requestCode == REQUEST_PERMISSION) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    anyDenied = true;
                    break;
                }
            }
            if (anyDenied) {
                MsgBox.show(MainActivity.this,
                        R.string.msg_denied_permission,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
            } else {
                initBluetooth();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    initView();
                } else {
                    MsgBox.show(MainActivity.this,
                            R.string.msg_enable_bluetooth,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                }
                break;
            case REQ_DISCOVERY_DEVICE:
                if (resultCode == Activity.RESULT_OK) {

                    mnuDiscoveryDevice.setVisible(false);
                    mnuReconnect.setVisible(false);
                    mnuDisconnect.setVisible(true);
                    txtConnState.setText(R.string.connection_state_connected);
                    btnInventory.setEnabled(true);
                    btnClear.setEnabled(true);
                    btnMask.setEnabled(false);
                    RemoteDevice device = data.getParcelableExtra(Const.REMOTE_DEVICE);
                    if (device != null) {
                        saveDevice(device);
                    }
                } else {
                    mnuDiscoveryDevice.setVisible(true);
                    mnuReconnect.setVisible(true);
                    mnuDisconnect.setVisible(false);
                    txtConnState.setText(R.string.connection_state_disconnected);
                    btnInventory.setEnabled(false);
                    btnClear.setEnabled(false);
                    btnMask.setEnabled(false);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @SuppressLint("MissingPermission")
    private void initBluetooth() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();//BluetoothAdapter 객체를 획득한다.
        if (btAdapter.isEnabled()) {//기기의 블루투스 상태가 on인 경우
            initView();
        } else {//기기의 블루투스 상태가 off인 경우
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //ACTION_REQUEST_ENABLE 활동 작업 : 사용자가 Bluetooth를 켤 수 있는 시스템 활동을 표시
            startActivityForResult(intent, REQUEST_ENABLE_BT);
            //startActivityForResult() 새 액티비티를 열어줌 + 결과값 전달 (쌍방향)
            //블루투스 기능이 활성화 되어있지 않다면 요청을 보낸다.
        }
    }

    private void initView() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                RemoteDevice device = null;

                if (mReader == null) {
                    mReader = Reader.getReader(MainActivity.this,
                            device, TIMEOUT);
                    if (mReader != null) {
                        if (!mReader.start()) {
                            btnDisconnect();

                        } else {
                            btnConnect();

                        }
                    }
                } else {

                }
            }
        }, 500);
    }

    private void initViewReconnect(){
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
                RemoteDevice device = loadDevice();

                if (mReader == null) {
                    mReader = Reader.getReader(MainActivity.this,
                            device, TIMEOUT);
                    if (mReader != null) {
                        if (!mReader.start()) {
                            btnDisconnect();

                        } else {
                            btnConnect();

                        }
                        mReader.setEventListener(MainActivity.this);
                        //이벤트 읽어오기
                    }
                } else {

                }
//            }
//        }, 500);
    }


    private class CheckTypeTask extends AsyncTask<Reader, Void, RemoteDevice> {


//        CustomDialog waitDialog = new CustomDialog(SubActivity.this);

        @Override
        protected void onPreExecute(){

            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Loading...");

            dialog.show();
        }

        @Override
        protected RemoteDevice doInBackground(Reader params[]) {
            //여기서 백그라운드 스레드 작동

            if (params == null || params.length < 0) {
                return null;
            }

            Reader mReader = params[0];
            RemoteDevice device = loadDevice();

                if (mReader != null) {
                    if (!mReader.start()) {
//                        btnDisconnect();

                        return null;
                    } else {
//                        btnConnect();

                    }
                    mReader.setEventListener(MainActivity.this);
                    //이벤트 읽어오기
                }

            return device;
        }

        @Override
        public void onPostExecute(RemoteDevice result){
            dialog.dismiss();
            if (result == null) {
                btnDisconnect();
                MsgBox.show(MainActivity.this, R.string.msg_fail_connect);
            } else {
                btnConnect();
//                Intent intent = new Intent();
//                intent.putExtra(Const.REMOTE_DEVICE, result);
//                setResult(Activity.RESULT_OK, intent);
//                finish();
            }
        }
    }

    private void btnDisconnect(){
        mnuDiscoveryDevice.setVisible(true);
        mnuReconnect.setVisible(true);
        mnuDisconnect.setVisible(false);
        txtConnState.setText(R.string.connection_state_disconnected);
        btnInventory.setEnabled(false);
        btnClear.setEnabled(false);
        btnMask.setEnabled(false);
    }

    private void btnConnect(){
        mnuDiscoveryDevice.setVisible(false);
        mnuReconnect.setVisible(false);
        mnuDisconnect.setVisible(true);
        txtConnState.setText(R.string.connection_state_connected);
        btnInventory.setEnabled(true);
        btnClear.setEnabled(true);
        btnMask.setEnabled(false);
    }


    private void connectDevice() {
        if ((mReader = Reader.getReader(this)) == null) {
            btnInventory.setEnabled(false);
            btnClear.setEnabled(false);
            btnMask.setEnabled(false);

            return;
        }
        RemoteDevice device = mReader.getRemoteDevice();
        if (device == null) {
            if (mReader != null) {
                mReader.stop();
                mReader.destroy();
                mReader = null;
                txtConnState.setText(R.string.connection_state_disconnected);
            }
            btnInventory.setEnabled(false);
            btnClear.setEnabled(false);
            btnMask.setEnabled(false);


            return;
        }
        mReader.setEventListener(this);

        btnInventory.setEnabled(true);
        btnClear.setEnabled(true);
        btnMask.setEnabled(false);

        saveDevice(device);

        Log.d(TAG, String.format(Locale.US, "DEBUG. SELECT DEVICE [[%s], [%s]]",
                device.getName(), device.getAddress()));
    }

    private void inventory() {
        if (mReader == null) {
            Log.e(TAG, String.format(Locale.US,
                    "ERROR. inventory() - Failed to invalid reader object"));
            return;
        }
        if (mReader.isOperationRunning()) {
            mReader.stopOperation();
            btnInventory.setText(R.string.action_inventory);
        } else {
            mReader.startInventory(true, false, true);
            btnInventory.setText(R.string.action_stop);
        }
        Log.i(TAG, String.format(Locale.US, "INFO. inventory()"));
    }

    private void addTagData(String data) {
        int pos = data.indexOf(";");
        if (pos >= 0) {
            String tag = data.substring(0, pos - 1);
            adpTags.add(tag);
            txtAllCount.setText("" + adpTags.getTotalCount());
            txtCount.setText("" + adpTags.getCount());
        }
    }




    private static final String PREF_NAME = "rfid_inventory_sample";
    private static final String LAST_ADDRESS = "last_dev_address";

    // Load Device from Share Preference
    private RemoteDevice loadDevice() {
        RemoteDevice loadDevice = null;
        SharedPreferences pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String address = pref.getString(LAST_ADDRESS, "");
        if (address != null && address.length() > 0) {
            BluetoothDevice btDevice = btAdapter.getRemoteDevice(address);
            if (btDevice != null) {
                loadDevice = RemoteDevice.makeBtSppDevice(btDevice);
            }
        }

        return loadDevice;
    }

    // Save Device to Share Preference
    private void saveDevice(RemoteDevice device) {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor writer = pref.edit();
        writer.putString(LAST_ADDRESS, device.getAddress());
        writer.commit();
    }


}



