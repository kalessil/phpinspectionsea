<?php

class MyClass
{
    public function method()
    {
        new <weak_warning descr="[EA] Class reference 'self' could be replaced by 'MyClass'">self</weak_warning>;
        <weak_warning descr="[EA] Class reference 'self' could be replaced by 'MyClass'">self</weak_warning>::CONSTANT;
        <weak_warning descr="[EA] Class reference 'self' could be replaced by 'MyClass'">self</weak_warning>::staticMethod();
        <weak_warning descr="[EA] Class reference 'self' could be replaced by 'MyClass'">self</weak_warning>::$staticProperty;

        <weak_warning descr="[EA] Class reference '__CLASS__' could be replaced by 'MyClass::class'">__CLASS__</weak_warning>;
        <weak_warning descr="[EA] Class reference 'self' could be replaced by 'MyClass'">self</weak_warning>::class;

        (new MyClass)::staticMethod();

        // False-positives: already fixed.
        new MyClass;
        MyClass::CONSTANT;
        MyClass::staticMethod();
        MyClass::$staticProperty;
        MyClass::class;
    }

    // Not applicable: anonymous class is another context.
    public function anonymousClass() {
        return new class {
            public function method() {
                new self;
                self::CONSTANT;
                self::staticMethod();
                self::$staticProperty;

                __CLASS__;
            }
        };
    }
}

// Not applicable: is not inside a class.
__CLASS__;

trait MyTrait
{
    public function method($object) {
        Something::method(__CLASS__, $object);
    }
}
