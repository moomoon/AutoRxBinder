package org.dxm.example;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;

import com.dxm.rxbinder.annotations.RxBind;

import java.io.IOException;
import java.util.List;


public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @RxBind(name = "methodBinding", exception = IllegalArgumentException.class)
    public void method(int MainActivity) throws IOException {

    }


    @RxBind
    public static <B extends Rect> List<B> add(List<B> list, B a) {
        list.add(a);
        return list;
    }

}
