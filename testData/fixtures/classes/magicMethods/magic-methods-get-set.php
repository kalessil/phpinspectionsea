<?php

class NoSet {
    public function <error descr="[EA] __get should have pair method __set.">__get</error> ($name)     {}
    public function <error descr="[EA] __isset should have pair method __set.">__isset</error> ($name) {}
    public function <error descr="[EA] __unset should have pair method __set.">__unset</error> ($name) {}
}

class NoIssetGet {
    public function
        <error descr="[EA] __set should have pair method __get."><error descr="[EA] __set should have pair method __isset.">__set</error></error> ($name, $value) {}
}