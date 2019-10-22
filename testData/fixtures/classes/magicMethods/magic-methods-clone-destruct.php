<?php

class MissingParent {
    public function __destruct() {}
    public function __clone() {}
}
class ClassWithValidMethods extends MissingParent {
    public function __destruct() {
        parent::__destruct();
    }
    public function __clone() {
        parent::__clone();
    }
}

class ClassWithStaticMethods extends MissingParent {
    public static function <error descr="[EA] __destruct cannot be static.">__destruct</error>() {
        parent::__destruct();
    }
    public static function <error descr="[EA] __clone cannot be static.">__clone</error>() {
        parent::__clone();
    }
}
class ClassWithReturningMethods extends MissingParent {
    public function __destruct() {
        parent::__destruct();
        <error descr="[EA] __destruct cannot return a value.">return '...';</error>
    }
    public function __clone() {
        parent::__clone();
        <error descr="[EA] __clone cannot return a value.">return '...';</error>
    }
}
class ClassWithParametrizedMethods extends MissingParent {
    public function <error descr="[EA] __destruct cannot accept arguments.">__destruct</error>($optional = '') {
        parent::__destruct();
    }
}
class ClassWithMissingParentMethodsCalls extends MissingParent {
    public function <error descr="[EA] __destruct is probably missing MissingParent::__destruct call.">__destruct</error>() {
    }
    public function <error descr="[EA] __clone is probably missing MissingParent::__clone call.">__clone</error>() {
    }
}
