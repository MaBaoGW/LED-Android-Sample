/*
 * Copyright (C) 2015-2016 Emanuel Moecklin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onegravity.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class ColorWheelComponent {

    private OnColorChangedListener mListener;

    final private int mInitialColor;
    final private boolean mUseOpacityBar;
    private int mNewColor;

    private ColorWheelView mColorPicker;
    private EditText mExactViewR;
    private EditText mExactViewG;
    private EditText mExactViewB;

    ColorWheelComponent(int initialColor, int newColor, boolean useOpacityBar, OnColorChangedListener listener) {
        mInitialColor = initialColor;
        mNewColor = newColor;
        mUseOpacityBar = useOpacityBar;
        mListener = listener;
    }

    @SuppressLint("InflateParams")
    View createView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_color_wheel, null);

        mColorPicker = (ColorWheelView) view.findViewById(R.id.picker);

        ValueBar valueBar = (ValueBar) view.findViewById(R.id.valuebar);
        if (valueBar != null) {
            mColorPicker.addValueBar(valueBar);
        }

        SaturationBar saturationBar = (SaturationBar) view.findViewById(R.id.saturationbar);
        if (saturationBar != null) {
            mColorPicker.addSaturationBar(saturationBar);
        }

        OpacityBar opacityBar = (OpacityBar) view.findViewById(R.id.opacitybar);
        if (opacityBar != null) {
            if (mUseOpacityBar) {
                mColorPicker.addOpacityBar(opacityBar);
            }
            opacityBar.setVisibility(mUseOpacityBar ? View.VISIBLE : View.GONE);
        }

        mColorPicker.setOldCenterColor(mInitialColor);
        mColorPicker.setColor(mNewColor);
        mColorPicker.setOnColorChangedListener(mListener);

        mExactViewR = (EditText) view.findViewById(R.id.exactR);
        mExactViewG = (EditText) view.findViewById(R.id.exactG);
        mExactViewB = (EditText) view.findViewById(R.id.exactB);

        InputFilter[] filters = new InputFilter[]{new InputFilter.LengthFilter(2)};
        mExactViewR.setFilters(filters);
        mExactViewG.setFilters(filters);
        mExactViewB.setFilters(filters);

        String[] colorComponents = Util.convertToARGB(mInitialColor);
        mExactViewR.setText(colorComponents[1]);
        mExactViewG.setText(colorComponents[2]);
        mExactViewB.setText(colorComponents[3]);

        if (mExactViewR != null) {
            mColorPicker.addRedText(mExactViewR);
        }

        if (mExactViewG != null) {
            mColorPicker.addGreenText(mExactViewG);
        }

        if (mExactViewB != null) {
            mColorPicker.addBlueText(mExactViewB);
        }

        return view;
    }

    void activate(Context context, int newColor) {
        mColorPicker.setColor(newColor);
    }

    void deactivate(Context context) {
        // do nothing
    }

    int getColor() {
        return mColorPicker.getColor();
    }

}
