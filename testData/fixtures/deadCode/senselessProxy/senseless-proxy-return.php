<?php

class SPRParent {
    public function identicalMethod($a) {
        return $a;
    }
    public function stricterMethod($a) {
        return $a;
    }
    public function weakerMethod($a): \RuntimeException {
        return $a;
    }
}

class SPRChild extends SPRParent {
    public function <weak_warning descr="[EA] 'identicalMethod' method can be dropped, as it only calls parent's one.">identicalMethod</weak_warning>($b) {
        return parent::identicalMethod($b);
    }
    public function stricterMethod($b): string {
        return parent::stricterMethod($b);
    }
    public function weakerMethod($b):  \Exception {
        return parent::weakerMethod($b);
    }
}