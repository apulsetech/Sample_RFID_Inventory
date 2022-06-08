package com.example.rfid_inventory_sample.adapters;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.apulsetech.lib.remote.type.RemoteDevice;
import com.example.rfid_inventory_sample.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceListAdapter extends BaseAdapter {

    private List<RemoteDevice> list;
    private Map<String, RemoteDevice> map;

    public DeviceListAdapter() {
        this.list = new ArrayList<>();
        this.map = new HashMap<>();
    }

    @SuppressLint("MissingPermission")
    public void add(BluetoothDevice btDevice) {
        RemoteDevice device;

        if (btDevice == null) {
            return;
        }

        String name = btDevice.getName();
        String addr = btDevice.getAddress();

        if (name == null || name.length() <= 0) {
            return;
        }

        if (this.map.containsKey(addr)) {
            device = this.map.get(addr);
            this.map.remove(addr);
            this.list.remove(device);
        }
        device = RemoteDevice.makeBtSppDevice(btDevice);
        this.map.put(addr, device);
        this.list.add(device);
    }

    public String getAddress(int position) {
        return this.list.get(position).getAddress();
    }

    public void clear() {
        this.list.clear();
        this.map.clear();
    }

    @Override
    public int getCount() {
        return list.size();
    }//Adapter.getCount() : 아이템 개수 반환

    @Override
    public RemoteDevice getItem(int position) {
        return list.get(position);
    }//Adapter.getItem() : 해당 위치 아이템 반환 함수

    @Override
    public long getItemId(int position) {
        return position;
    }//Adapter.getItemId() : 해당 위치 반환 함수

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (null == convertView) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.item_bt_dev_list, null);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.display(list.get(position));
        return convertView;
    }//Adapter.getView() : 해당 위치 뷰 반환 함수

    class ViewHolder {
        private TextView txtName;
        private TextView txtAddress;

        public ViewHolder(View parent) {
            txtName = parent.findViewById(R.id.name);
            txtAddress = parent.findViewById(R.id.address);
            parent.setTag(this);
        }

        public void display(RemoteDevice device) {
            txtName.setText(device.getName());
            txtAddress.setText(device.getAddress());
        }
    }
}
