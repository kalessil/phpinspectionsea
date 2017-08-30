<?php

class MyClass
{
    public function method() {
        new self;
        self::CONSTANT;
        self::staticMethod();
        self::$staticProperty;

        __CLASS__;

        (new self)::staticMethod();

        // False-positives:
        self::class;
        static::class;
    }

    // Not applicable: anonymous class is another context.
    public function anonymousClass() {
        return new class {
            public function method() {
                new MyClass;
                MyClass::CONSTANT;
                MyClass::staticMethod();
                MyClass::$staticProperty;

                MyClass::class;
            }
        };
    }
}

trait MyTrait
{
    public function method($object) {
        Something::method(MyTrait::class, $object);
    }
}
