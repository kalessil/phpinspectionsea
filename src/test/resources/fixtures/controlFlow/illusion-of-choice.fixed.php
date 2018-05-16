<?php

class CasesHolder {

    public function ternaries($x, $y) {
        $y;

        $y;
        $x;
        $y;
        $x;

        $x;
        $x;
        0;
        0;

        /* false-positives */
        $x === $y ? $x : $x;
        $x !== $y ? $x : $x;
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
}