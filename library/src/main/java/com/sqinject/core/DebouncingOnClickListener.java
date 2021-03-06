package com.sqinject.core;

import android.view.View;

public abstract class DebouncingOnClickListener implements View.OnClickListener{

    @Override
    public void onClick(View view) {
        doClick(view);
    }

    public abstract void doClick(View view);

}
