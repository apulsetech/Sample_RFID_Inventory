package com.example.rfid_inventory_sample.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.rfid_inventory_sample.MainActivity;
import com.example.rfid_inventory_sample.R;
import com.example.rfid_inventory_sample.items.TagItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TagListAdapter extends BaseAdapter {

    private ArrayList<TagItem> list;
    private Map<String, TagItem> map;
    private int totalCount;

//    TextView textAllCount = ((MainActivity)MainActivity.mCOntext).txtAllCount;
//    TextView textCount = ((MainActivity)MainActivity.mCOntext).txtCount;


    public TagListAdapter() {
        this.list = new ArrayList<>();
        this.map = new HashMap<>();
        totalCount = 0;
    }

    public void add(String tag){
        TagItem item = null;

        if (this.map.containsKey(tag)) {
            item = this.map.get(tag);
            item.increamentCount();
        } else {
            item = new TagItem(tag);
            this.list.add(item);
            this.map.put(tag, item);
        }
        totalCount++;
        notifyDataSetChanged();
    }

    public void clear(){
        this.list.clear();
        this.map.clear();
        totalCount = 0;



        notifyDataSetChanged();
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TagListAdapter.ViewHolder holder = null;
        if (null == convertView) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.item_tag_list, null);
            holder = new TagListAdapter.ViewHolder(convertView);
        } else {
            holder = (TagListAdapter.ViewHolder) convertView.getTag();
        }
        holder.display(list.get(position));
        return convertView;
    }

    public void add(String address, String tagData) {
    }

    class ViewHolder{
        private TextView txtTag;
        private TextView txtCount;

    public ViewHolder(View parent){
        txtTag= parent.findViewById(R.id.tag);
        txtCount = parent.findViewById(R.id.count);
        parent.setTag(this);
    }

        public void display(TagItem item) {
            txtTag.setText(item.getTag());
            txtCount.setText("" + item.getCount());
        }
    }
}
