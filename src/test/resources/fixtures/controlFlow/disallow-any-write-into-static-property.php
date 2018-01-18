<?php

class ExistingClass
{
    public static $staticProperty;

    public function method()
    {
        echo self::$staticProperty;
        echo static::$staticProperty;

        <weak_warning descr="Static properties should not be modified.">self::$staticProperty          = 'whatever'</weak_warning>;
        <weak_warning descr="Static properties should not be modified.">ExistingClass::$staticProperty = 'whatever'</weak_warning>;
        <weak_warning descr="Static properties should not be modified.">static::$staticProperty        = 'whatever'</weak_warning>;
    }
}


<weak_warning descr="Static properties should not be modified.">ExistingClass::$staticProperty = 'whatever'</weak_warning>;
<weak_warning descr="Static properties should not be modified.">MissingClass::$staticProperty  = 'whatever'</weak_warning>;

echo ExistingClass::$staticProperty;