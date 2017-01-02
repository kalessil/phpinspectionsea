<?php

    function EMOTIONALFUNCTION() {
    }

    class class1 {
        public function calmmethod() {
            <weak_warning descr="Name provided in this call should be 'EMOTIONALFUNCTION' (case mismatch).">emotionalfunction()</weak_warning>;
        }
    }

    $obj = new class1();
    <weak_warning descr="Name provided in this call should be 'calmmethod' (case mismatch).">$obj->calmMethod()</weak_warning>;