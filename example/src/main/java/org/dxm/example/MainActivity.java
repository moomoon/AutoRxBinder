package org.dxm.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dxm.rxbinder.annotations.RxBind;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @RxBind(name = "methodBinding")
    public void method(int MainActivity) throws IOException {

    }

}
