<?php

class MyClass
{
    public function method() {
        new <weak_warning descr="Class reference 'MyClass' could be replaced by 'self'">MyClass</weak_warning>;
        <weak_warning descr="Class reference 'MyClass' could be replaced by 'self'">MyClass</weak_warning>::CONSTANT;
        <weak_warning descr="Class reference 'MyClass' could be replaced by 'self'">MyClass</weak_warning>::staticMethod();
        <weak_warning descr="Class reference 'MyClass' could be replaced by 'self'">MyClass</weak_warning>::$staticProperty;

        <weak_warning descr="Class reference 'MyClass::class' could be replaced by '__CLASS__'">MyClass::class</weak_warning>;

        (new self)::staticMethod();

        /* false-positives */
        self::class;
        static::class;
        (function(){ return new MyClass(); })();
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

class MyException extends \Exception
{
    public function method() {
        return new class() extends MyException {};
    }
}