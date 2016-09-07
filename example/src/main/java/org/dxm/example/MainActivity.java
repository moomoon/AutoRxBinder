package org.dxm.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.dxm.rxbinder.annotations.Partial;
import com.dxm.rxbinder.annotations.RxBind;

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
    public void testAction(String a, @Partial Object b, List<Map<String, String>> c) {
    }

    @RxBind(name = "newArray")
    public void testAction(String a) {

    }

    @RxBind
    public static String test() {
        return null;

    }

    @RxBind
    public static Integer test(String param0, String param1, @Partial Integer param2){
        return 0;
    }
}
