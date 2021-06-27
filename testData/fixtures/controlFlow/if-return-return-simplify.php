<?php

class CasesHolder {
    public function ifReturnReturnOne($x) {
        <warning descr="[EA] The construct can be replaced with 'return !($x > 0)'.">if</warning> ($x > 0) { return false; }
        return true;
    }
    public function ifReturnReturnTwo($x) {
        <warning descr="[EA] The construct can be replaced with 'return $x > 0'.">if</warning> ($x > 0) { return true; }
        return false;
    }
    public function ifReturnReturnThree($x) {
        if ($x === 0) { $x = 0; }
        <warning descr="[EA] The construct can be replaced with 'return $x > 0'.">if</warning> ($x > 0) { return true; }
        return false;
    }
    public function ifReturnReturnFour($x) {
        if ($x === 0) { return true; }
        /* a comment here */
        if ($x > 0) { return true; }
        return false;
    }
    public function ifReturnReturnFive($x) {
        if ($x > 0) { return true; }
        return true;
    }

    public function ifReturnElseReturnOne($x) {
        <warning descr="[EA] The construct can be replaced with 'return $x > 0'.">if</warning> ($x > 0) { return true; }
        else { return false; }
    }
    public function ifReturnElseReturnTwo($x) {
        <warning descr="[EA] The construct can be replaced with 'return !($x > 0)'.">if</warning> ($x > 0) { return false; }
        else { return true; }
    }
    public function ifReturnElseReturnThree($x) {
        if ($x > 0) { return true; }
        else { return true; }
    }
    public function ifReturnElseReturnFour($x) {
        if ($x === 0) { return true; }
        /* a comment here */
        <warning descr="[EA] The construct can be replaced with 'return !($x > 0)'.">if</warning> ($x > 0) { return false; }
        else { return true; }
    }

    public function ifReturnReturnResultSimplification($x) {
        <warning descr="[EA] The construct can be replaced with 'return !($x === 0)'.">if</warning> ($x === 0) { return false; }
        return true;

        <warning descr="[EA] The construct can be replaced with 'return !($x !== 0)'.">if</warning> ($x !== 0) { return false; }
        return true;

        <warning descr="[EA] The construct can be replaced with 'return !($x == 0)'.">if</warning> ($x == 0) { return false; }
        return true;

        <warning descr="[EA] The construct can be replaced with 'return !($x != 0)'.">if</warning> ($x != 0) { return false; }
        return true;

        <warning descr="[EA] The construct can be replaced with 'return !($x > 0)'.">if</warning> ($x > 0) { return false; }
        return true;

        <warning descr="[EA] The construct can be replaced with 'return !($x >= 0)'.">if</warning> ($x >= 0) { return false; }
        return true;

        <warning descr="[EA] The construct can be replaced with 'return !($x < 0)'.">if</warning> ($x < 0) { return false; }
        return true;

        <warning descr="[EA] The construct can be replaced with 'return !($x <= 0)'.">if</warning> ($x <= 0) { return false; }
        return true;
    }
}
