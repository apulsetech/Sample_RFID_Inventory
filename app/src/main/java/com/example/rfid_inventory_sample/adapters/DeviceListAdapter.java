package com.example.rfid_inventory_sample.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apulsetech.lib.remote.type.RemoteDevice;
import com.example.rfid_inventory_sample.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceListAdapter extends BaseAdapter {

    private List<RemoteDevice> items;
    private Map<String, RemoteDevice> itemFilter;
    private RecyclerView recyclerView;

    public DeviceListAdapter() {
        this.items = new ArrayList<>();
        this.itemFilter = new HashMap<>();
        this.recyclerView = null;
    }

    public void add(RemoteDevice device) {
        String addr = device.getAddress();

        if (this.itemFilter.containsKey(addr)) {
            device = this.itemFilter.get(addr);
            this.itemFilter.remove(addr);
            this.items.remove(device);
        }
        this.itemFilter.put(addr, device);
        this.items.add(device);
    }

    public String getAddress(int position) {
        return this.items.get(position).getAddress();
    }

    public void clear() {
        this.items.clear();
        this.itemFilter.clear();
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public RemoteDevice getItem(int position) {
        return this.items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_bluetooth_device, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.display(this.items.get(position));
        return convertView;
    }

    class ViewHolder {
        private TextView txtName;
        private TextView txtAddress;

        public ViewHolder(@NonNull View itemView){
            txtName = itemView.findViewById(R.id.name);
            txtAddress = itemView.findViewById(R.id.address);
            itemView.setTag(this);
        }

        public void display(RemoteDevice device) {
            txtName.setText(device.getName());
            txtAddress.setText(device.getAddress());
        }
    }
}
