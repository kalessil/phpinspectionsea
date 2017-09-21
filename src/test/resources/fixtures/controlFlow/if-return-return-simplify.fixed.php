<?php

class CasesHolder {
    public function ifReturnReturnOne($x) {
        if ($x > 0) { return false; }
        return true;
    }
    public function ifReturnReturnTwo($x) {
        if ($x > 0) { return true; }
        return false;
    }
    public function ifReturnReturnThree($x) {
        if ($x === 0) { return true; }
        /* a comment here */
        if ($x > 0) { return true; }
        return false;
    }
    public function ifReturnReturnFour($x) {
        if ($x > 0) { return true; }
        return true;
    }

    public function ifReturnElseReturnOne($x) {
        if ($x > 0) { return true; }
        else { return false; }
    }
    public function ifReturnElseReturnTwo($x) {
        if ($x > 0) { return false; }
        else { return true; }
    }
    public function ifReturnElseReturnThree($x) {
        if ($x > 0) { return true; }
        else { return true; }
    }
    public function ifReturnElseReturnFour($x) {
        if ($x === 0) { return true; }
        /* a comment here */
        if ($x > 0) { return false; }
        else { return true; }
    }
}
