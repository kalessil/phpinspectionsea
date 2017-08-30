<?php

class MyClass
{
    public function method()
    {
        new MyClass;
        MyClass::CONSTANT;
        MyClass::staticMethod();
        MyClass::$staticProperty;

        MyClass::class;
        MyClass::class;

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
