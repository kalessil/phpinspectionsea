<?php

    class ClassToString {}
    $obj = new \stdClass();
    <weak_warning descr="Please use '(string) $obj' instead.">$obj->__toString()</weak_warning>;

    class ClassImplicitMagicMethodsCall {
        public function createFromWhatever($object) {
            <weak_warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__construct()</weak_warning>;
            <weak_warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__destruct()</weak_warning>;
            <weak_warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__call()</weak_warning>;
            <weak_warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__callStatic()</weak_warning>;
            <weak_warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__get()</weak_warning>;
            <weak_warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__set()</weak_warning>;
            <weak_warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__isset()</weak_warning>;
            <weak_warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__unset()</weak_warning>;
            <weak_warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__sleep()</weak_warning>;
            <weak_warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__wakeup()</weak_warning>;
            <weak_warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__invoke()</weak_warning>;
            <weak_warning descr="Implicit magic method calls should be avoided as these methods are used by PHP internals.">$object->__set_state()</weak_warning>;

            <weak_warning descr="Please use '(string) $object' instead.">$object->__toString()</weak_warning>;

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