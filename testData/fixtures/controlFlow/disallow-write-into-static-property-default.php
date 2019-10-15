<?php

use BaseClass as ClassAlias;

class BaseClass
{
    public static $staticProperty;
    public static $overridden;

    public function method()
    {
        self::$staticProperty       = 'whatever';
        static::$staticProperty     = 'whatever';
        BaseClass::$staticProperty  = 'whatever';
        ClassAlias::$staticProperty = 'whatever';

        /* case with a lambda, the context is a function */
        return function () {
            <weak_warning descr="[EA] Static properties should be modified only inside the source class.">BaseClass::$staticProperty  = 'whatever'</weak_warning>;
            <weak_warning descr="[EA] Static properties should be modified only inside the source class.">ClassAlias::$staticProperty = 'whatever'</weak_warning>;
        };
    }
}

class ChildClass extends BaseClass
{
    public static $overridden = 'overridden';

    public function method()
    {
        ChildClass::$overridden = 'whatever';
        self::$overridden       = 'whatever';
        static::$overridden     = 'whatever';

        <weak_warning descr="[EA] Static properties should be modified only inside the source class.">ClassAlias::$overridden     = 'whatever'</weak_warning>;
        <weak_warning descr="[EA] Static properties should be modified only inside the source class.">ClassAlias::$staticProperty = 'whatever'</weak_warning>;
        <weak_warning descr="[EA] Static properties should be modified only inside the source class.">ChildClass::$staticProperty = 'whatever'</weak_warning>;
    }
}

/* all static field writes in non-method context being reported */
class ClassWithoutFields {}
<weak_warning descr="[EA] Static properties should be modified only inside the source class.">BaseClass::$staticProperty  = 'whatever'</weak_warning>;
<weak_warning descr="[EA] Static properties should be modified only inside the source class.">ChildClass::$staticProperty = 'whatever'</weak_warning>;
<weak_warning descr="[EA] Static properties should be modified only inside the source class.">ClassAlias::$staticProperty = 'whatever'</weak_warning>;
<weak_warning descr="[EA] Static properties should be modified only inside the source class.">ClassWithoutFields::$missingField = 'whatever'</weak_warning>;
<weak_warning descr="[EA] Static properties should be modified only inside the source class.">MissingClass::$missingField       = 'whatever'</weak_warning>;