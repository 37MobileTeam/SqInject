package com.sqinject.test_lib;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.sqinject.annotation.BindColor;
import com.sqinject.annotation.BindId;
import com.sqinject.annotation.BindInt;
import com.sqinject.annotation.BindString;
import com.sqinject.annotation.BindView;
import com.sqinject.annotation.IdType;
import com.sqinject.core.SqInject;

public class TestActivity extends Activity {

    @BindView(SqR.id.dialog)
    Button mDialogTest;

    @BindView(SqR.id.fragment)
    Button mFragmentTest;

    @BindString(SqR.string.app_name)
    String mAppName;

    @BindInt(SqR.integer.appId)
    int mAppId;

    @BindColor(SqR.color.white)
    int whiteColor;

    @BindView(SqR.id.imgIcon)
    ImageView mImageView;

    @BindId(value = SqR.drawable.icon, type = IdType.DRAWABLE)
    int iconId;

    @BindId(value = SqR.layout.activity_test, type = IdType.LAYOUT)
    int layoutId;

    @BindId(value = SqR.id.container, type = IdType.ID)
    int containerId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SqInject.bindIds(this,this);
        setContentView(layoutId);
        SqInject.bindView(this);
        //String测试
        LogUtils.d("mAppName: " + mAppName + ", mAppId: " + mAppId + ", whiteColor: " + whiteColor);
        //activity测试
        Toast.makeText(this, mDialogTest.getText().toString(), Toast.LENGTH_SHORT).show();

        mDialogTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog();
            }
        });

        mFragmentTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment();
            }
        });

        mImageView.setImageResource(iconId);
    }

    //fragment测试
    private void fragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        TestFragment testFragment = new TestFragment();
        transaction.add(containerId, testFragment);
        transaction.commit();
    }

    //dialog测试
    private void dialog() {
        new TestDialog(this).show();
    }


}
