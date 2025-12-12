<?php

class MyClass
{
    public function method(<weak_warning descr="[EA] Replace class reference 'self' with 'MyClass' to follow common code style. Also, check the inspection settings to promote using 'self' instead.">self</weak_warning> $parameter): <weak_warning descr="[EA] Replace class reference 'self' with 'MyClass' to follow common code style. Also, check the inspection settings to promote using 'self' instead.">self</weak_warning> {
        new <weak_warning descr="[EA] Replace class reference 'self' with 'MyClass' to follow common code style. Also, check the inspection settings to promote using 'self' instead.">self</weak_warning>;
        <weak_warning descr="[EA] Replace class reference 'self' with 'MyClass' to follow common code style. Also, check the inspection settings to promote using 'self' instead.">self</weak_warning>::CONSTANT;
        <weak_warning descr="[EA] Replace class reference 'self' with 'MyClass' to follow common code style. Also, check the inspection settings to promote using 'self' instead.">self</weak_warning>::staticMethod();
        <weak_warning descr="[EA] Replace class reference 'self' with 'MyClass' to follow common code style. Also, check the inspection settings to promote using 'self' instead.">self</weak_warning>::$staticProperty;

        <weak_warning descr="[EA] Replace class reference '__CLASS__' with 'MyClass::class' to follow common code style. Also, check the inspection settings to promote using 'self' instead.">__CLASS__</weak_warning>;
        <weak_warning descr="[EA] Replace class reference 'self' with 'MyClass' to follow common code style. Also, check the inspection settings to promote using 'self' instead.">self</weak_warning>::class;

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
