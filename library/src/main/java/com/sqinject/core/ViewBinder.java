package com.sqinject.core;

import android.view.View;

public interface ViewBinder<T> {

    void bindView(T target, View source);

}
