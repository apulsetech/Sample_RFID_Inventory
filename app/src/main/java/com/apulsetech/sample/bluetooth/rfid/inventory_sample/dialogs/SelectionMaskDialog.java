package com.apulsetech.sample.bluetooth.rfid.inventory_sample.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.apulsetech.lib.rfid.type.SelectionCriterias;
import com.apulsetech.sample.bluetooth.rfid.inventory_sample.R;

import java.util.Locale;

public class SelectionMaskDialog {
    public static void show(Context context, int title,
                            SelectionCriterias.Criteria criteria,
                            OnSaveSelectionMaskListener listener) {
        final String[] banks = context.getResources().getStringArray(R.array.memory_bank);
        final String[] targets = context.getResources().getStringArray(R.array.mask_target);
        final String[] sessionActions = context.getResources().getStringArray(R.array.mask_action_session);
        final String[] selectActions = context.getResources().getStringArray(R.array.mask_action_select);
        final SelectMask mask = new SelectMask(criteria);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_select_mask, null);
        final TextView txtBank = view.findViewById(R.id.bank);
        final TextView txtTarget = view.findViewById(R.id.target);
        final TextView txtAction = view.findViewById(R.id.action);
        final EditText edtOffset = view.findViewById(R.id.offset);
        final EditText edtLength = view.findViewById(R.id.length);
        final EditText edtMask = view.findViewById(R.id.mask);

        txtBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleNameValueListDialog.show(context,
                        R.string.select_mask_bank,
                        mask.getBank(), banks, new int[] {
                                SelectionCriterias.Bank.FILE_TYPE,
                                SelectionCriterias.Bank.EPC,
                                SelectionCriterias.Bank.TID,
                                SelectionCriterias.Bank.USER,
                        }, new SingleNameValueListDialog.OnSelectValueListener() {
                            @Override
                            public void onSelectValue(int value) {
                                mask.setBank(value);
                                txtBank.setText(banks[mask.getBank()]);
                            }
                        });
            }
        });
        txtTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleNameValueListDialog.show(context,
                        R.string.select_mask_target,
                        mask.getTarget(), targets, new int[]{
                                SelectionCriterias.Target.INVENTORIED_S0,
                                SelectionCriterias.Target.INVENTORIED_S1,
                                SelectionCriterias.Target.INVENTORIED_S2,
                                SelectionCriterias.Target.INVENTORIED_S3,
                                SelectionCriterias.Target.SELECTED,
                        }, new SingleNameValueListDialog.OnSelectValueListener() {
                            @Override
                            public void onSelectValue(int value) {
                                mask.setTarget(value);
                                txtTarget.setText(targets[mask.getTarget()]);
                                txtAction.setText(
                                        mask.getTarget() == SelectionCriterias.Target.SELECTED ?
                                                selectActions[mask.getAction()] :
                                                sessionActions[mask.getAction()]);
                            }
                        });
            }
        });
        txtAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleNameValueListDialog.show(context,
                        R.string.select_mask_action,
                        mask.getAction(),
                        mask.getTarget() == SelectionCriterias.Target.SELECTED ?
                                selectActions : sessionActions,
                        new int[]{
                                SelectionCriterias.Action.ASLINVA_DSLINVB,
                                SelectionCriterias.Action.ASLINVA_NOTHING,
                                SelectionCriterias.Action.NOTHING_DSLINVB,
                                SelectionCriterias.Action.NSLINVS_NOTHING,
                                SelectionCriterias.Action.DSLINVB_ASLINVA,
                                SelectionCriterias.Action.DSLINVB_NOTHING,
                                SelectionCriterias.Action.NOTHING_ASLINVA,
                                SelectionCriterias.Action.NOTHING_NSLINVS,
                        }, new SingleNameValueListDialog.OnSelectValueListener() {
                            @Override
                            public void onSelectValue(int value) {
                                mask.setAction(value);
                                txtAction.setText(
                                        mask.getTarget() == SelectionCriterias.Target.SELECTED ?
                                        selectActions[mask.getAction()] :
                                                sessionActions[mask.getAction()]);
                            }
                        });
            }
        });
        txtBank.setText(banks[mask.getBank()]);
        txtTarget.setText(targets[mask.getTarget()]);
        txtAction.setText(mask.getTarget() == SelectionCriterias.Target.SELECTED ?
                selectActions[mask.getAction()] : sessionActions[mask.getAction()]);
        edtOffset.setText(String.format(Locale.US, "%d", criteria.getOffset()));
        edtLength.setText(String.format(Locale.US, "%d", criteria.getLength()));
        edtMask.setText(criteria.getMask());

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mask.setOffset(Integer.parseInt(edtOffset.getText().toString()));
                mask.setLength(Integer.parseInt(edtLength.getText().toString()));
                mask.setMask(edtMask.getText().toString());
                if (listener != null)
                    listener.onSaveSelectionMask(mask.getCriteria());
            }
        });
        builder.setNegativeButton(R.string.action_cancel, null);
        builder.show();
    }

    public interface OnSaveSelectionMaskListener {
        void onSaveSelectionMask(SelectionCriterias.Criteria criteria);
    }

    private static class SelectMask {
        private int bank;
        private int target;
        private int action;
        private int offset;
        private int length;
        private String mask;

        public SelectMask(SelectionCriterias.Criteria criteria) {
            this.bank = criteria.getBank();
            this.target = criteria.getTarget();
            this.action = criteria.getAction();
            this.offset = criteria.getOffset();
            this.length = criteria.getLength();
            this.mask = criteria.getMask();
        }

        public int getBank() {
            return bank;
        }

        public void setBank(int bank) {
            this.bank = bank;
        }

        public int getTarget() {
            return target;
        }

        public void setTarget(int target) {
            this.target = target;
        }

        public int getAction() {
            return action;
        }

        public void setAction(int action) {
            this.action = action;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public String getMask() {
            return mask;
        }

        public void setMask(String mask) {
            this.mask = mask;
        }

        public SelectionCriterias.Criteria getCriteria() {
            return new SelectionCriterias.Criteria(this.target, this.bank,
                    this.mask, this.offset, this.length, this.action);
        }
    }
}
