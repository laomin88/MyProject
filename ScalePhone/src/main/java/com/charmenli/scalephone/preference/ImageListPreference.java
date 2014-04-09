package com.charmenli.scalephone.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.widget.ListAdapter;

public class ImageListPreference extends ListPreference {

    private ListAdapter mListAdapter=null;

    private int mClickedDialogEntryIndex;

    public ImageListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageListPreference(Context context) {
        super(context);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        mClickedDialogEntryIndex = findIndexOfValue(getValue());
        builder.setAdapter(mListAdapter,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mClickedDialogEntryIndex != which) {
                            mClickedDialogEntryIndex = which;
                            ImageListPreference.this.notifyChanged();
                        }
                        ImageListPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                        dialog.dismiss();
                    }
                }
        );
        builder.setPositiveButton(null, null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        CharSequence[] entryValues = getEntryValues();
        if (positiveResult && mClickedDialogEntryIndex >= 0 && entryValues != null) {
            String value = entryValues[mClickedDialogEntryIndex].toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
            setSummary(getEntry());
        }
    }

    public void setListAdapter(ListAdapter listAdapter) {
        this.mListAdapter = listAdapter;
        setSummary(getEntry());
    }

    public ListAdapter getListAdapter() {
        return this.mListAdapter;
    }

}