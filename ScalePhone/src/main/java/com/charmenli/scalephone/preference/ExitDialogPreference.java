package com.charmenli.scalephone.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.charmenli.scalephone.R;

/**
 * Created by charmenli on 2014/4/8.
 */
public class ExitDialogPreference extends DialogPreference {
    private OnExitListener mExitListener;
    public ExitDialogPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ExitDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public interface OnExitListener {
        void doExit();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setTitle(getDialogTitle())
                .setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mExitListener != null) {
                            mExitListener.doExit();
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getNegativeButtonText(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    public void setOnExitListener(OnExitListener listener) {
        this.mExitListener = listener;
    }

    public OnExitListener getOnExitListener() {
        return this.mExitListener;
    }
}
