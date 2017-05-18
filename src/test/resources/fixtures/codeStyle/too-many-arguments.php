<?php

    function <warning descr="Too many parameters">threeParameters</warning> ($one, $two, $three) {}
    class ClassTooManyParameters {
        public function __construct($one, $two, $three) {}
        public function <warning descr="Too many parameters">threeParameters</warning> ($one, $two, $three) {}
    }

    /* false-positives */
    function noParameters()            {}
    function oneParameter($one)        {}
    function twoParameters($one, $two) {}
    class ClassNotTooManyParameters {
        public function noParameters()            {}
        public function oneParameter($one)        {}
        public function twoParameters($one, $two) {}
    }

    class PhpUnitTest {
        public function threeParameters ($one, $two, $three) {}
    }