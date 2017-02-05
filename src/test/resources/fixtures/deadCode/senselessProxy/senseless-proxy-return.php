<?php

class SPRParent {
    public function method($a) {
        return $a;
    }
}

class SPRChild extends SPRParent {
    public function <weak_warning descr="'method' method can be dropped, as it only calls parent's one.">method</weak_warning>($b) {
        return parent::method($b);
    }
}