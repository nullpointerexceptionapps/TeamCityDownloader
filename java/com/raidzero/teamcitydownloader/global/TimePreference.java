package com.raidzero.teamcitydownloader.global;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;


public class TimePreference extends Preference {
    private static final String tag = "TimePreference";

    private String currentInterval;
    private TextView txt_batteryWarning;

    private NumberPicker mMinPicker;
    private NumberPicker mHourPicker;

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.time_picker);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        currentInterval = getPersistedString("2:0");

        final NumberPicker hourPicker = (NumberPicker) view.findViewById(R.id.numPicker_hour);
        final NumberPicker minPicker = (NumberPicker) view.findViewById(R.id.numPicker_minute);
        txt_batteryWarning = (TextView) view.findViewById(R.id.view_interval_battery_warning);

        mMinPicker = minPicker;
        mHourPicker = hourPicker;

        // disable direct text editing
        mMinPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mHourPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        minPicker.setMinValue(0);
        minPicker.setMaxValue(59);

        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);

        hourPicker.setValue(getHour());
        minPicker.setValue(getMinute());


        getValue();

        hourPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Debug.Log(tag, String.format("onValueChange(hour, %d, %d)", oldVal, newVal));

                if (minPicker.getValue() == 0 && newVal == 0) {
                    minPicker.setValue(1);
                }

                persistString(getValue());
            }
        });

        minPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Debug.Log(tag, String.format("onValueChange(min, %d, %d)", oldVal, newVal));

                if (hourPicker.getValue() == 0 && newVal == 0) {
                    hourPicker.setValue(1);
                }

                persistString(getValue());
            }
        });
    }

    private String getValue() {

        if (mHourPicker.getValue() == 0 && mMinPicker.getValue() < 10) {
            txt_batteryWarning.setVisibility(View.VISIBLE);
        } else {
            txt_batteryWarning.setVisibility(View.GONE);
        }

        int currentHour = mHourPicker.getValue();
        int currentMinute = mMinPicker.getValue();

        Debug.Log(tag, currentHour + ":" + currentMinute);
        return currentHour + ":" + currentMinute;
    }

    private int getHour() {
        return Integer.valueOf(currentInterval.split(":")[0]);
    }

    private int getMinute() {
        return Integer.valueOf(currentInterval.split(":")[1]);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            currentInterval = getPersistedString("2:0");
        } else {
            currentInterval = defaultValue.toString();
            persistString(currentInterval);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.currentInterval = currentInterval;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        currentInterval = myState.currentInterval;
        notifyChanged();
    }

    private static class SavedState extends BaseSavedState {

        String currentInterval;

        private SavedState(Parcel source) {
            super(source);

            currentInterval = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeString(currentInterval);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Creator<SavedState> CREATOR =
            new Creator<SavedState>() {
                @Override
                public SavedState createFromParcel(Parcel source) {
                    return new SavedState(source);
                }

                @Override
                public SavedState[] newArray(int size) {
                    return new SavedState[size];
                }
            };
    }
}
