<?php

class ArrayCastingEquivalents
{
    public function ifCaseArrayBrackets($array) {
        if (!is_array($array)) {
            $array = [$array];
        }
        return $array;
    }
    public function ifCaseArrayLiteral($array) {
        if (!is_array($array)) {
            $array = [$array];
        }
        return $array;
    }

    public function ternaryCase($array) {
        $array = is_array($array)  ? $array : [$array];
        return $array;
    }
    public function invertedTernaryCase($array) {
        $array = !is_array($array) ? [$array] : $array;
        return $array;
    }
}


