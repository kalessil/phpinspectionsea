<?php

class ArrayCastingEquivalents
{
    public function ifCaseArrayBrackets($array) {
        <weak_warning descr="'(array) ...' construct can probably be used (can change code behaviour).">if</weak_warning> (!is_array($array)) {
            $array = [$array];
        }
        return $array;
    }
    public function ifCaseArrayLiteral($array) {
        <weak_warning descr="'(array) ...' construct can probably be used (can change code behaviour).">if</weak_warning> (!is_array($array)) {
            $array = array($array);
        }
        return $array;
    }

    public function ternaryCase($array) {
        $array = <weak_warning descr="'(array) ...' construct can probably be used (can change code behaviour).">is_array($array)  ? $array : [$array]</weak_warning>;
        return $array;
    }
    public function invertedTernaryCase($array) {
        $array = <weak_warning descr="'(array) ...' construct can probably be used (can change code behaviour).">!is_array($array) ? [$array] : $array</weak_warning>;
        return $array;
    }

    /* false-positives */
    public function objectCasting(\stdClass $subject) {
        $result = !is_array($subject) ? [$subject] : $subject;
    }
}
