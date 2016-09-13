package org.dxm.example;

import android.app.Activity;
import android.os.Bundle;

import com.dxm.rxbinder.annotations.RxBind;

import java.io.IOException;
import java.util.List;

import rx.functions.Func2;


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
    public static <A> List<A> add(List<A> list, A a) {
        list.add(a);
        return list;
    }

}
