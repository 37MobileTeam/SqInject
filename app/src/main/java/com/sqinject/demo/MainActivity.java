package com.sqinject.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.sqinject.annotation.BindView;
import com.sqinject.annotation.OnClick;
import com.sqinject.core.SqInject;
import com.sqinject.test_lib.TestActivity;

public class MainActivity extends AppCompatActivity {

    @BindView("tv")
    TextView hello;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SqInject.bind(this);
        Log.e("SqInject", hello.getText().toString());
    }

    @OnClick(SqR.id.tv)
    public void click(View view) {
        Intent intent = new Intent(MainActivity.this, TestActivity.class);
        startActivity(intent);
    }
}
