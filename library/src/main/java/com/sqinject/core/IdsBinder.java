package com.sqinject.core;


import android.content.Context;

public interface IdsBinder<T>  {
    void bind(T target, Context context);
}
