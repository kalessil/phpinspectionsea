<?php

class ExistingClass
{
    public static $staticProperty;

    public function method()
    {
        echo self::$staticProperty;
        echo static::$staticProperty;

        <warning descr="Static properties should not be modified.">self::$staticProperty          = 'whatever'</warning>;
        <warning descr="Static properties should not be modified.">ExistingClass::$staticProperty = 'whatever'</warning>;
        <warning descr="Static properties should not be modified.">static::$staticProperty        = 'whatever'</warning>;
    }
}


<warning descr="Static properties should not be modified.">ExistingClass::$staticProperty = 'whatever'</warning>;
<warning descr="Static properties should not be modified.">MissingClass::$staticProperty  = 'whatever'</warning>;

echo ExistingClass::$staticProperty;