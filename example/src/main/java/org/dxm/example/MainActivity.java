package org.dxm.example;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;

import com.dxm.rxbinder.annotations.Partial;
import com.dxm.rxbinder.annotations.RxBind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.functions.Func1;


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

    @RxBind
    public <A, B> List<B> map(List<A> list, @Partial Func1<A, B> transform) {
        List<B> result = new ArrayList<>();
        for (A a: list) result.add(transform.call(a));
        return result;
    }
    @RxBind
    public static <A, B> List<B> map1(List<A> list, @Partial Func1<A, B> transform) {
        List<B> result = new ArrayList<>();
        for (A a: list) result.add(transform.call(a));
        return result;
    }

}
