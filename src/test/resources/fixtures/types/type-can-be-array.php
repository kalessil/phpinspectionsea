<?php

    interface IRoot {
        public function <weak_warning descr="Parameter $x can be declared as 'array $x'.">isArray</weak_warning>($x = array());
    }
    interface IChild extends IRoot {
        public function <weak_warning descr="Parameter $x can be declared as 'array $x'.">isArray</weak_warning>($x = array());
    }
    abstract class CRoot {
        public abstract function <weak_warning descr="Parameter $x can be declared as 'array $x'.">min</weak_warning>($x = []);
        public abstract function <weak_warning descr="Parameter $x can be declared as 'array $x'.">avg</weak_warning>($x = array());
        public abstract function <weak_warning descr="Parameter $x can be declared as 'array $x'.">fill</weak_warning>(&$x = array(), $value);
    }

    /* false-positive: interface is inherited with its' issues */
    class CChild extends CRoot implements IChild
    {
        public function isArray($x = array())       {}
        public function min($x = [])                {}
        public function avg($x = array())           {}
        public function fill(&$x = array(), $value) {}
    }

    /* false-positive: mixed types */
    /** @param null|array $x */
    function mixed_types($x = []) {}
