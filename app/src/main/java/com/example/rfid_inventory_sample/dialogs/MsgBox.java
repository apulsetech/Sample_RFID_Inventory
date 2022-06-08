package com.example.rfid_inventory_sample.dialogs;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.example.rfid_inventory_sample.R;

public class MsgBox {
    public interface OnClickListener {
        void onPositiveClick();
        void onCancel();
    }

    public static void show(Context context, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.action_ok, null);
        builder.show();
    }

    public static void show(Context context, String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.action_ok, null);
        builder.show();
    }

    public static void show(Context context, String msg,
                            AlertDialog.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.action_ok, listener);
        builder.show();
    }

    public static void showInfo(Context context, String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setPositiveButton(R.string.action_ok, null);
        builder.show();
    }

    public static void showInfo(Context context, String msg,
                                AlertDialog.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setPositiveButton(R.string.action_ok, listener);
        builder.show();
    }

    public static void showInfo(Context context, String title, String msg,
                                AlertDialog.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setPositiveButton(R.string.action_ok, listener);
        builder.show();
    }

    public static void showError(Context context, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(R.string.action_ok, null);
        builder.show();
    }

    public static void showError(Context context, String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(R.string.action_ok, null);
        builder.show();
    }

    public static void showError(Context context, String msg,
                                 AlertDialog.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(R.string.action_ok, listener);
        builder.show();
    }

    public static void showError(Context context, String title, String msg,
                                 AlertDialog.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(R.string.action_ok, listener);
        builder.show();
    }

    public static void showQuestion(Context context, String msg,
                                    AlertDialog.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_menu_help);
        builder.setPositiveButton(R.string.action_yes, listener);
        builder.setNegativeButton(R.string.action_no, listener);
        builder.show();
    }

    public static void showQuestion(Context context, String title, String msg,
                                    AlertDialog.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setIcon(android.R.drawable.ic_menu_help);
        builder.setPositiveButton(R.string.action_yes, listener);
        builder.setNegativeButton(R.string.action_no, listener);
        builder.show();
    }

    public static void show(Context context, int msg) {
        if (msg <= 0) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.action_ok, null);
        builder.show();
    }

    public static void show(Context context, int title, int msg) {
        if (msg <= 0) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != 0) builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.action_ok, null);
        builder.show();
    }

    public static void show(Context context, int msg,
                            AlertDialog.OnClickListener listener) {
        if (msg <= 0) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.action_ok, listener);
        builder.show();
    }

    public static void show(Context context, int title, int msg,
                            AlertDialog.OnClickListener listener) {
        if (msg <= 0) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != 0) builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.action_ok, listener);
        builder.show();
    }

    public static void showInfo(Context context, int msg) {
        if (msg <= 0) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setPositiveButton(R.string.action_ok, null);
        builder.show();
    }

    public static void showInfo(Context context, int title, int msg) {
        if (msg <= 0) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != 0) builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setPositiveButton(R.string.action_ok, null);
        builder.show();
    }

    public static void showInfo(Context context, int msg,
                                AlertDialog.OnClickListener listener) {
        if (msg <= 0) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setPositiveButton(R.string.action_ok, listener);
        builder.show();
    }

    public static void showInfo(Context context, int title, int msg,
                                AlertDialog.OnClickListener listener) {
        if (msg <= 0) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != 0) builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setPositiveButton(R.string.action_ok, listener);
        builder.show();
    }

    public static void showError(Context context, int msg) {
        if (msg <= 0) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(R.string.action_ok, null);
        builder.show();
    }

    public static void showError(Context context, int title, int msg) {
        if (msg <= 0) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != 0) builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(R.string.action_ok, null);
        builder.show();
    }

    public static void showError(Context context, int msg,
                                 AlertDialog.OnClickListener listener) {
        if (msg <= 0) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(R.string.action_ok, listener);
        builder.show();
    }

    public static void showError(Context context, int title, int msg,
                                 AlertDialog.OnClickListener listener) {
        if (msg <= 0) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != 0) builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(R.string.action_ok, listener);
        builder.show();
    }

    public static void showError(Context context, int title, int msg,
                                 final OnClickListener listener) {
        if (msg <= 0) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != 0) builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null)
                    listener.onPositiveClick();
            }
        });
        builder.show();
    }

    public static void showQuestion(Context context, int msg,
                                    AlertDialog.OnClickListener listener) {
        if (msg <= 0) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_menu_help);
        builder.setPositiveButton(R.string.action_yes, listener);
        builder.setNegativeButton(R.string.action_no, listener);
        builder.show();
    }

    public static void showQuestion(Context context, int title, int msg,
                                    AlertDialog.OnClickListener listener) {
        if (msg <= 0) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != 0) builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_menu_help);
        builder.setPositiveButton(R.string.action_yes, listener);
        builder.setNegativeButton(R.string.action_no, listener);
        builder.show();
    }
}
