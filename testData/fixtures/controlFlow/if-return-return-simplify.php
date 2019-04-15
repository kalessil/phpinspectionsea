<?php

class CasesHolder {
    public function ifReturnReturnOne($x) {
        <warning descr="The construct can be replaced with 'return !($x > 0)'.">if</warning> ($x > 0) { return false; }
        return true;
    }
    public function ifReturnReturnTwo($x) {
        <warning descr="The construct can be replaced with 'return $x > 0'.">if</warning> ($x > 0) { return true; }
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
        <warning descr="The construct can be replaced with 'return $x > 0'.">if</warning> ($x > 0) { return true; }
        else { return false; }
    }
    public function ifReturnElseReturnTwo($x) {
        <warning descr="The construct can be replaced with 'return !($x > 0)'.">if</warning> ($x > 0) { return false; }
        else { return true; }
    }
    public function ifReturnElseReturnThree($x) {
        if ($x > 0) { return true; }
        else { return true; }
    }
    public function ifReturnElseReturnFour($x) {
        if ($x === 0) { return true; }
        /* a comment here */
        <warning descr="The construct can be replaced with 'return !($x > 0)'.">if</warning> ($x > 0) { return false; }
        else { return true; }
    }

    public function ifNotReturnElseReturn($x): bool {
        <warning descr="The construct can be replaced with 'return !$this->ifNotReturnElseReturn()'.">if</warning> (!$this->ifNotReturnElseReturn()) { return true; }
        else { return false; }
        <warning descr="The construct can be replaced with 'return $this->ifNotReturnElseReturn()'.">if</warning> (!$this->ifNotReturnElseReturn()) { return false; }
        else { return true; }

        <warning descr="The construct can be replaced with 'return $this->ifNotReturnElseReturn()'.">if</warning> ($this->ifNotReturnElseReturn()) { return true; }
        else { return false; }
        <warning descr="The construct can be replaced with 'return !($this->ifNotReturnElseReturn())'.">if</warning> ($this->ifNotReturnElseReturn()) { return false; }
        else { return true; }
    }

    function ifAssignElseAssignReturn($x) {
        <warning descr="The construct can be replaced with 'return $x === 0'.">if</warning> ($x === 0) {
            $result = true;
        } else {
            $result = false;
        }
        return $result;
    }

    function assignIfAssignReturn($x) {
        $result = false;
        <warning descr="The construct can be replaced with 'return $x === 0'.">if</warning> ($x === 0) {
            $result = true;
        }
        return $result;
    }
}
