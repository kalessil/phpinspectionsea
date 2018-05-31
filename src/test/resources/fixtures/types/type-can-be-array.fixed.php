<?php

    interface IRoot {
        public function isArray(array $x = array());
    }
    interface IChild extends IRoot {
        public function isArray(array $x = array());
    }
    abstract class CRoot {
        public abstract function min(array $x = []);
        public abstract function avg(array $x = array());
        public abstract function fill(array &$x = array(), $value);
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
