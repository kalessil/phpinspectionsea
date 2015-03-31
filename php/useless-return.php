<?php

class a {
    public function __construct()   { return 1; }
    public function __destruct()    { return 1; }
    public function __set()         { return 1; }
    public function __clone()       { return 1; }
    public function __unset()       { return 1; }
    public function __get()         { return; }
}

function f1() { echo 123; return $a = 5; }
function f2() { $x = 123; return $x++; }
function f3() { echo 123; return; }