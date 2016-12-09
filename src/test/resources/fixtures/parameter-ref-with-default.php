<?php

    function func(<weak_warning descr="Usually a default value is not needed in this scenario.">&$param1 = null</weak_warning>) {}

    abstract class AClass implements AnInterface {
        abstract function
            method1(<weak_warning descr="Usually a default value is not needed in this scenario.">&$param1 = null</weak_warning>);
        public function
            method2(<weak_warning descr="Usually a default value is not needed in this scenario.">&$param2 = null</weak_warning>) {}
    }

    interface AnInterface {
        public function
            method2(<weak_warning descr="Usually a default value is not needed in this scenario.">&$param2 = null</weak_warning>);
    }

    trait ATrait {
        public function
            method(<weak_warning descr="Usually a default value is not needed in this scenario.">&$param1 = null</weak_warning>, $param2 = null) {}
    }