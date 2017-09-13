<?php

    function Case_Mismatch_Function() {}

    class CaseMismatchClass {
        public function CaseMismatchMethod() {
            Case_Mismatch_Function();
        }
    }

    $object = new CaseMismatchClass();
    $object->CaseMismatchMethod();

    /* false-positives: functions aliasing */
    use function \trim as trimm;
    echo trimm('  ');