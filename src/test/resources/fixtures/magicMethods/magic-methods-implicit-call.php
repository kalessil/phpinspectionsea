<?php

    class ClassToString {}
    $obj = new \stdClass();
    <warning descr="Please use '(string) $obj' instead.">$obj->__toString()</warning>;

    class ClassImplicitMagicMethodsCall {
        public function createFromWhatever($object) {
            <warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__construct()</warning>;
            <warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__destruct()</warning>;
            <warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__call()</warning>;
            <warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__callStatic()</warning>;
            <warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__get()</warning>;
            <warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__set()</warning>;
            <warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__isset()</warning>;
            <warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__unset()</warning>;
            <warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__sleep()</warning>;
            <warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__wakeup()</warning>;
            <warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__invoke()</warning>;
            <warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__set_state()</warning>;

            <warning descr="Please use '(string) $object' instead.">$object->__toString()</warning>;

            $this->__construct();
            $this->__toString();
        }
    }

    /* false-positives */
    /* unserialize invokes construction of the object: the right way doing the things */
    class ClassUnserialize {
        public function unserialize() {
            $this->__construct();
        }
    }
    /* call a higher-level parent method */
    class SecondLevelException extends RuntimeException {
        public function __construct($message = '', $code = 0, Exception $previous = null) {
            \Exception::__construct($message, $code, $previous);
        }
    }