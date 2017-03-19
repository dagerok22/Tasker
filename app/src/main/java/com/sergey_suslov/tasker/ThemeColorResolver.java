package com.sergey_suslov.tasker;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;

/**
 * Created by sergey on 19.03.17.
 */

public class ThemeColorResolver {
    private Context ctx;
    private TypedValue typedValue;
    public ThemeColorResolver(Context ctx) {
        this.ctx = ctx;
        TypedValue typedValue = new TypedValue();
    }

    public int getAccentColor(){
        ctx.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        return typedValue.data;
    }

    public int getPrimaryColor(){
        ctx.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    public int getPrimaryDarkColor(){
        ctx.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        return Color.parseColor(String.format("#%06X", (0xFFFFFF & typedValue.data)));
    }

    public int getPrimaryActiveColor(){
        ctx.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        return Color.parseColor("#50FFFFFF");
    }

    public int getPrimaryPassiveColor(){
        ctx.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        return typedValue.data;
    }
}
