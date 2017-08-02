<?php

class NoSet {
    public function <error descr="__get should have pair method __set.">__get</error> ($name)     {}
    public function <error descr="__isset should have pair method __set.">__isset</error> ($name) {}
    public function <error descr="__unset should have pair method __set.">__unset</error> ($name) {}
}

class NoIssetGet {
    public function
        <error descr="__set should have pair method __isset."><error descr="__set should have pair method __get.">__set</error></error> ($name, $value) {}
}