<?php

class ClassWithValidMethods extends MissingClass {
    public function __destruct() {
        parent::__destruct();
    }
    public function __clone() {
        parent::__clone();
    }
}

class ClassWithStaticMethods extends MissingClass {
    public static function <error descr="[EA] __destruct cannot be static.">__destruct</error>() {
        parent::__destruct();
    }
    public static function <error descr="[EA] __clone cannot be static.">__clone</error>() {
        parent::__clone();
    }
}

class ClassWithReturningMethods extends MissingClass {
    public function __destruct() {
        parent::__destruct();
        <error descr="[EA] __destruct cannot return a value.">return '...';</error>
    }
    public function __clone() {
        parent::__clone();
        <error descr="[EA] __clone cannot return a value.">return '...';</error>
    }
}

class ClassWithParametrizedMethods extends MissingClass {
    public function <error descr="[EA] __destruct cannot accept arguments.">__destruct</error>($optional = '') {
        parent::__destruct();
    }
    public function <error descr="[EA] __clone cannot accept arguments.">__clone</error>($optional = '') {
        parent::__clone();
    }
}

class ClassWithMissingParentMethodsCalls extends MissingClass {
    public function <error descr="[EA] __destruct cannot accept arguments.">__destruct</error>($optional = '') {
    }
    public function <error descr="[EA] __clone cannot accept arguments.">__clone</error>($optional = '') {
    }
}
