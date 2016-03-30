<?php

    class Clonable {
        public function __clone() {
        }
        public function __toString() {
            return '';
        }
    }

    $obj = new Clonable();
    $obj->__toString();            // <- reported

    class Dirty {
        public function __construct() {
        }

        public function createFromWhatever() {
            $this->__construct();  // <- reported
        }
    }