<?php

class CasesHolder {

    public function ternaries() {
        ($x === $y) ? $x : <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">$y</warning>;

        $x === $y ? $x : <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">$y</warning>;
        $x !== $y ? <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">$x</warning> : $y;
        $x == $y ? $x : <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">$y</warning>;
        $x != $y ? <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">$x</warning> : $y;

        $x === 0 ? 0 : <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">$x</warning>;
        $x !== 0 ? <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">$x</warning> : 0;
        $x === 0 ? $x : <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">0</warning>;
        $x !== 0 ? <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">0</warning> : $x;

        $x === $y ? $x : <warning descr="Same value is in the alternative variant. It's possible to simplify the construct.">$x</warning>;
        $x !== $y ? <warning descr="Same value is in the alternative variant. It's possible to simplify the construct.">$x</warning> : $x;

        /* false-positives */
        $x >= $y ? $x : $y;
        $x ? $x : $y;
    }

    public function conditionals() {

    }

}