<?php

class ClassWithValidSetState {
    public static function __set_state($array) {
        return new ClassWithValidSetState();
    }
}


class ClassWithNoReturnSetState {
    public static function <error descr="[EA] __set_state must return \ClassWithNoReturnSetState (resolved: '').">__set_state</error> ($array) {
    }
}

class ClassWithInvalidReturnSetState {
    public static function __set_state($array) {
        <error descr="[EA] __set_state must return \ClassWithInvalidReturnSetState (resolved: '\stdClass').">return new stdClass();</error>
    }
}

class ClassWithReturnStaticSetState {
    public static function __set_state($array) {
        return new static();
    }
}

class ClassWithNoParametersSetState {
    public static function <error descr="[EA] __set_state accepts exactly 1 arguments.">__set_state</error> () {
        return new ClassWithNoParametersSetState();
    }
}

class ClassWithNonPublicSetState {
    private static function <error descr="[EA] __set_state must be public.">__set_state</error> ($array) {
        return new ClassWithNonPublicSetState();
    }
}

class ClassWithNonStaticSetState {
    public function <error descr="[EA] __set_state must be static.">__set_state</error> ($array) {
        return new ClassWithNonStaticSetState();
    }
}