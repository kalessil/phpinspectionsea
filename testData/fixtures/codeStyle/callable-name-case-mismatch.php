<?php

    function Case_Mismatch_Function() {}

    class CaseMismatchClass {
        public function CaseMismatchMethod() {
            <weak_warning descr="Name provided in this call should be 'Case_Mismatch_Function' (case mismatch).">case_mismatch_function()</weak_warning>;
        }
    }

    $object = new CaseMismatchClass();
    <weak_warning descr="Name provided in this call should be 'CaseMismatchMethod' (case mismatch).">$object->casemismatchmethod()</weak_warning>;

    /* false-positives: functions aliasing */
    use function \trim as trimm;
    echo trimm('  ');