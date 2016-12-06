<?php

    class ClassToString {}
    $obj = new \stdClass();
    <warning descr="Please use (string) $obj instead">$obj->__toString()</warning>;

    class ClassImplicitMagicMethodsCall {
        public function createFromWhatever() {
            $this-><warning descr="Implicit magic method calls shall be avoided as these methods are used by PHP internals.">__construct</warning> ();
            $this-><warning descr="Implicit magic method calls shall be avoided as these methods are used by PHP internals.">__destruct</warning> ();
            $this-><warning descr="Implicit magic method calls shall be avoided as these methods are used by PHP internals.">__call</warning> ();
            $this-><warning descr="Implicit magic method calls shall be avoided as these methods are used by PHP internals.">__callStatic</warning> ();
            $this-><warning descr="Implicit magic method calls shall be avoided as these methods are used by PHP internals.">__get</warning> ();
            $this-><warning descr="Implicit magic method calls shall be avoided as these methods are used by PHP internals.">__set</warning> ();
            $this-><warning descr="Implicit magic method calls shall be avoided as these methods are used by PHP internals.">__isset</warning> ();
            $this-><warning descr="Implicit magic method calls shall be avoided as these methods are used by PHP internals.">__unset</warning> ();
            $this-><warning descr="Implicit magic method calls shall be avoided as these methods are used by PHP internals.">__sleep</warning> ();
            $this-><warning descr="Implicit magic method calls shall be avoided as these methods are used by PHP internals.">__wakeup</warning> ();
            $this-><warning descr="Implicit magic method calls shall be avoided as these methods are used by PHP internals.">__toString</warning> ();
            $this-><warning descr="Implicit magic method calls shall be avoided as these methods are used by PHP internals.">__invoke</warning> ();
            $this-><warning descr="Implicit magic method calls shall be avoided as these methods are used by PHP internals.">__set_state</warning> ();
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