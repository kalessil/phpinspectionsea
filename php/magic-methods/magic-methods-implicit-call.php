<?php

    class Clonable {
        public function __clone() {
        }
        public function __toString() {
            return '';
        }
    }

    $obj = new Clonable();
    $obj->__toString();