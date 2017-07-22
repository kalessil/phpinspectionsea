<?php

class MyClass {
    public function method() {
        new <weak_warning descr="Class reference \"MyClass\" could be replaced by \"self\"">MyClass</weak_warning>;
        <weak_warning descr="Class reference \"MyClass\" could be replaced by \"self\"">MyClass</weak_warning>::CONSTANT;
        <weak_warning descr="Class reference \"MyClass\" could be replaced by \"self\"">MyClass</weak_warning>::staticMethod();
        <weak_warning descr="Class reference \"MyClass\" could be replaced by \"self\"">MyClass</weak_warning>::$staticProperty;
    }
}
