<?php

    class ClassToString {}
    $obj = new \stdClass();
    (string)$obj;

    class ClassImplicitMagicMethodsCall {
        public function createFromWhatever($object) {
            $object->__construct();
            $object->__destruct();
            $object->__call();
            $object->__callStatic();
            $object->__get();
            $object->__set();
            $object->__isset();
            $object->__unset();
            $object->__sleep();
            $object->__wakeup();
            $object->__invoke();
            $object->__set_state();

            (string)$object;

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