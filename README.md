# AutoRxBinder
Annotation processor to generate [RxJava](https://github.com/ReactiveX/RxJava) Func/Action bindings for java 6+.

## Usage
Simply include AutoRxBinder in your project and annotate the methods you want to use in RxJava with `@RxBind`.
```java
public class Foo {
    @RxBind
    public void hello(String name) {
        System.out.println("Hello, " + name + ".");
    }
}
```
Now you can use it like:
```java

public void baz() {
    Foo foo = new Foo();
    Observable.just("world").subscribe(FooBindings.hello(foo));
}
```
Or better:
```java
import static package.of.Foo.FooBindings.hello;
...
public void baz() {
    Foo foo = new Foo();
    Observable.just("world").subscribe(hello(foo));
}
```
###@Partial
Annotate parameters you want to pass to Func/Action constructor with @Partial:
```java
public class Foo {
    @RxBind
    public void greet(@Partial String niceThings, String name) {
        System.out.println(niceThings + ", " + name + ".");
    }
}
```
And use it like:
```java
import static package.of.Foo.FooBindings.greet;
...
public void baz() {
    Foo foo = new Foo();
    Observable.just("sekai").subscribe(greet(foo, "Hello"));
}
```

##Setup
In your module gradle dependencies:
```groovy
apt 'com.github.moomoon:AutoRxBinder:0.1.0'
compileOnly 'com.github.moomoon:AutoRxBinder:0.1.0'
```
If you are using Android Studio, use: 
```groovy
apt 'com.github.moomoon:AutoRxBinder:0.1.0'
provided 'com.github.moomoon:AutoRxBinder:0.1.0'
```


