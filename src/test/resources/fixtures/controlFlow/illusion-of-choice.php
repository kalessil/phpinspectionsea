<?php

class CasesHolder {

    public function ternaries($x, $y) {
        ($x === $y) ? $x : <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">$y</warning>;

        $x === $y ? $x : <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">$y</warning>;
        $x !== $y ? <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">$x</warning> : $y;
        $x == $y ? $x : <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">$y</warning>;
        $x != $y ? <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">$x</warning> : $y;

        $x === 0 ? 0 : <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">$x</warning>;
        $x !== 0 ? <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">$x</warning> : 0;
        $x === 0 ? $x : <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">0</warning>;
        $x !== 0 ? <warning descr="Actually the same value is in the alternative variant. It's possible to simplify the construct.">0</warning> : $x;

        /* false-positives */
        $x === $y ? $x : $x;
        $x !== $y ? $x : $x;
        $x >= $y ? $x : $y;
        $x ? $x : $y;
    }

    public function conditionals($x, $y) {
        if ($x === $y) {
            return $x;
        } else {
            return <warning descr="Actually the same value gets returned by the alternative return. It's possible to simplify the construct.">$y</warning>;
        }

        if ($x !== $y) {
            return <warning descr="Actually the same value gets returned by the alternative return. It's possible to simplify the construct.">$x</warning>;
        } else {
            return $y;
        }

        if ($x === $y) {
            return $x;
        }
        return <warning descr="Actually the same value gets returned by the alternative return. It's possible to simplify the construct.">$y</warning>;

        if ($x !== $y) {
            return <warning descr="Actually the same value gets returned by the alternative return. It's possible to simplify the construct.">$x</warning>;
        }
        return $y;

        if ($x === $y) {
            return $x;
        } else {
            return <warning descr="Same value gets returned by the alternative return. It's possible to simplify the construct.">$x</warning>;
        }

        if ($x === $y) {
            return $x;
        }
        return <warning descr="Same value gets returned by the alternative return. It's possible to simplify the construct.">$x</warning>;


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