package org.dxm.aptest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.dxm.rxbinder.RxBind;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @RxBind
    public String test(String parameter) {
        return parameter;
    }

    @RxBind
    public void testAction(String a, Object b, List<Map<String, String>> c) {
    }
}
