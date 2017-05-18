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
test();
test(1);
test();

$myClass->test();
$myClass->test(1);
$myClass->test();
