package com.apulsetech.sample.bluetooth.rfid.inventory_sample.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.apulsetech.lib.rfid.type.SelectionCriterias;
import com.apulsetech.sample.bluetooth.rfid.inventory_sample.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MaskListAdapter extends BaseAdapter {

    private List<SelectionCriterias.Criteria> list;

    private final String[] MEM_BANK;
    private final String[] TARGET;
    private final String[] ACTION_INVENTORY;
    private final String[] ACTION_SELECT;

    private OnCheckedChangeListener listener;

    public MaskListAdapter(Context context) {
        this.list = new ArrayList<>();
        MEM_BANK = context.getResources().getStringArray(R.array.memory_bank);
        TARGET = context.getResources().getStringArray(R.array.mask_target);
        ACTION_INVENTORY = context.getResources().getStringArray(R.array.mask_action_session);
        ACTION_SELECT = context.getResources().getStringArray(R.array.mask_action_select);

        listener = null;
    }

    public void setOnItemCheckedChangeListener(OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    public void addAllItems(SelectionCriterias criterias) {
        for (SelectionCriterias.Criteria item : criterias.getCriteria()) {
            this.list.add(item);
        }
        notifyDataSetChanged();
    }

    public void addItem(SelectionCriterias.Criteria criteria) {
        this.list.add(criteria);
        notifyDataSetChanged();
    }

    public void updateItem(int position, SelectionCriterias.Criteria criteria) {
        this.list.set(position, criteria);
        notifyDataSetChanged();
    }

    public void remoteItem(int[] indices) {
        for (int i = indices.length - 1; i >= 0; i--) {
            this.list.remove(indices[i]);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        this.list.clear();
        notifyDataSetChanged();
    }

    public SelectionCriterias getSelectionCrierias() {
        SelectionCriterias criterias = new SelectionCriterias();
        for (SelectionCriterias.Criteria criteria : this.list) {
            criterias.add(criteria);
        }
        return criterias;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public SelectionCriterias.Criteria getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (null == convertView) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.item_select_mask, null);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.display(position, this.list.get(position));
        return convertView;
    }

    class ViewHolder implements CompoundButton.OnCheckedChangeListener {
        private int position;
        private TextView txtBank;
        private TextView txtTarget;
        private TextView txtAction;
        private TextView txtOffset;
        private TextView txtLength;
        private TextView txtMask;

        public ViewHolder(View parent) {
            position = -1;
            CheckBox checkBox = parent.findViewById(R.id.checkbox);
            checkBox.setOnCheckedChangeListener(this);
            txtBank = parent.findViewById(R.id.select_mask_bank);
            txtTarget = parent.findViewById(R.id.select_mask_target);
            txtAction = parent.findViewById(R.id.select_mask_action);
            txtOffset = parent.findViewById(R.id.select_mask_offset);
            txtLength = parent.findViewById(R.id.select_mask_length);
            txtMask = parent.findViewById(R.id.select_mask_data);
            parent.setTag(this);
        }

        public void display(int position, SelectionCriterias.Criteria item) {
            this.position = position;
            txtBank.setText(MEM_BANK[item.getBank()]);
            txtTarget.setText(TARGET[item.getTarget()]);
            if (item.getTarget() == SelectionCriterias.Target.SELECTED) {
                txtAction.setText(ACTION_SELECT[item.getAction()]);
            } else {
                txtAction.setText(ACTION_INVENTORY[item.getAction()]);
            }
            txtOffset.setText(String.format(Locale.US, "%d bits", item.getOffset()));
            txtLength.setText(String.format(Locale.US, "%d bits", item.getLength()));
            txtMask.setText(item.getMask());
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (MaskListAdapter.this.listener != null) {
                MaskListAdapter.this.listener.onCheckedChanged(this.position, isChecked);
            }
        }
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(int position, boolean isChecked);
    }
}
