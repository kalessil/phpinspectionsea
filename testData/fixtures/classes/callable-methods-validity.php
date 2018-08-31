<?php

class TestClass
{
    public function method()              {}
    private function privateMethod()      {}
    static public function staticMethod() {}
}

var_dump(
    is_callable('strtolower'),

    is_callable(<warning descr="'method' should be static (e.g. $this usage in static context provokes fatal errors).">[TestClass::class, 'method']</warning>),
    is_callable([TestClass::class, 'staticMethod']),

    is_callable(<warning descr="'method' should be static (e.g. $this usage in static context provokes fatal errors).">['TestClass', 'method']</warning>),
    is_callable(['TestClass', 'staticMethod']),

    is_callable(['TestClass::method']),
    is_callable(['TestClass::staticMethod']),

    is_callable([new TestClass(),  'staticMethod']),
    is_callable([new TestClass(),  'method']),
    is_callable(<warning descr="'privateMethod' should be public (e.g. $this usage in static context provokes fatal errors).">[new TestClass(),  'privateMethod']</warning>)
);