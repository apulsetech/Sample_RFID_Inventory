package com.example.rfid_inventory_sample.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rfid_inventory_sample.R;
import com.example.rfid_inventory_sample.items.TagItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TagListAdapter extends BaseAdapter {

    private ArrayList<TagItem> items;
    private Map<String, TagItem> tagItems;
    private int totalCount;

    public TagListAdapter() {
        this.items = new ArrayList<>();
        this.tagItems = new HashMap<>();
        this.totalCount = 0;
    }

    public void add(String tag){
        TagItem item = null;

        if (this.tagItems.containsKey(tag)) {
            item = this.tagItems.get(tag);
            item.increamentCount();
        } else {
            item = new TagItem(tag);
            this.items.add(item);
            this.tagItems.put(tag, item);
        }
        this.totalCount++;
        notifyDataSetChanged();
    }

    public void clear(){
        this.items.clear();
        this.tagItems.clear();
        this.totalCount = 0;
        notifyDataSetChanged();
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public TagItem getItem(int position) {
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
            convertView = inflater.inflate(R.layout.item_tag_list, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.display(this.items.get(position));
        return convertView;
    }

    class ViewHolder {
        private TextView txtTag;
        private TextView txtCount;

    public ViewHolder(@NonNull View itemView){
        txtTag= itemView.findViewById(R.id.tag);
        txtCount = itemView.findViewById(R.id.count);
        itemView.setTag(this);
    }

        public void display(TagItem item) {
            txtTag.setText(item.getTag());
            txtCount.setText("" + item.getCount());
        }
    }
}
