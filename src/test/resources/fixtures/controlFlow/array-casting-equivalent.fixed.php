<?php

class ArrayCastingEquivalents
{
    public function ifCaseArrayBrackets($array) {
        $array = (array)$array
        return $array;
    }
    public function ifCaseArrayLiteral($array) {
        $array = (array)$array
        return $array;
    }

    public function ternaryCase($array) {
        $array = (array)$array;
        return $array;
    }
    public function invertedTernaryCase($array) {
        $array = (array)$array;
        return $array;
    }
}
