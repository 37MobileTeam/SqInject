package com.sqinject.test_lib;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sqinject.annotation.BindInt;
import com.sqinject.annotation.BindView;
import com.sqinject.core.SqInject;

public class TestDialog extends Dialog {

    @BindView("test")
    Button testBtn;

    @BindInt("appId")
    int mAppId;

    public TestDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_test);
        SqInject.bind(this);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
