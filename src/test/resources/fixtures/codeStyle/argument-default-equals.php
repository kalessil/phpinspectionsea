<?php

function test($a = 0, $b = 0) {
}

abstract class MyAbstract {
    public function test($a = 0, $b = 0) {
    }
}

class MyClass extends MyAbstract {
}

// Objects.
$myClass = new MyClass;

// Positives.
test();
test(1);
test(0, 1);

$myClass->test();
$myClass->test(1);
$myClass->test(0, 1);

// Warnings.
test(<weak_warning descr="The argument can be safely dropped, as identical to the default value.">0</weak_warning>);
test(1, <weak_warning descr="The argument can be safely dropped, as identical to the default value.">0</weak_warning>);
test(<weak_warning descr="The argument can be safely dropped, as identical to the default value.">0, 0</weak_warning>);

$myClass->test(<weak_warning descr="The argument can be safely dropped, as identical to the default value.">0</weak_warning>);
$myClass->test(1, <weak_warning descr="The argument can be safely dropped, as identical to the default value.">0</weak_warning>);
$myClass->test(<weak_warning descr="The argument can be safely dropped, as identical to the default value.">0, 0</weak_warning>);
