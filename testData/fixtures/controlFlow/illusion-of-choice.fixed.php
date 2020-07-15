<?php

class CasesHolder {

    public function ternaries($x, $y, $z) {
        /* pattern: can be simplified to a condition operand */
        $y;

        $y;
        $x;
        $y;
        $x;

        $x;
        $x;
        0;
        0;

        /* pattern: identical branches */
        $x ? $y : $y;

        /* false-positives */
        $x === $y ? $x : $z;
        $x !== $y ? $x : $z;
        $x >= $y ? $x : $y;
        $x ? $x : $y;
    }

    public function conditionals($x, $y) {
        return $y;

        return $x;

        return $y;

        return $x;

        return $x;

        return $x;


        /* false-positives */
        if ($x === $y) {
            $this->ternaries($x, $y);
            return $x;
        } else {
            return $y;
        }

        if ($x === $y) {
            $this->ternaries($x, $y);
            return $x;
        }
        return $y;
    }

    public function falsy($parameter) {
        if ($parameter == null) {
            return null;
        } else {
            return $parameter;
        }

        if ($parameter != null) {
            return $parameter;
        }
        return null;
    }
}