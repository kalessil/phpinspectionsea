<?php

class NonNullParamsDefaultsParent {
    public function
        method(
            $text,
            <weak_warning descr="[EA] Null should be used as the default value (nullable types are the goal, right?)">$x = 0</weak_warning>,
            <weak_warning descr="[EA] Null should be used as the default value (nullable types are the goal, right?)">$y = 0</weak_warning>,
            $z = null,
            <weak_warning descr="[EA] Null should be used as the default value (nullable types are the goal, right?)">$style = []</weak_warning>
        ) {}

    private function a(<weak_warning descr="[EA] Null should be used as the default value (nullable types are the goal, right?)">$a = ''</weak_warning>) {}
}

class NonNullParamsDefaultsChild extends NonNullParamsDefaultsParent {
    public function method($text, $x = 0, $y = 0, $z = null, $style = []) {}

    private function a(<weak_warning descr="[EA] Null should be used as the default value (nullable types are the goal, right?)">$a = ''</weak_warning>) {}
}

function a(<weak_warning descr="[EA] Null should be used as the default value (nullable types are the goal, right?)">$a = ''</weak_warning>) {}
function b(<weak_warning descr="[EA] Null should be used as the default value (nullable types are the goal, right?)">?string $a = ''</weak_warning>) {}
/** @param string|null $a */
function c(<weak_warning descr="[EA] Null should be used as the default value (nullable types are the goal, right?)">$a = ''</weak_warning>) {}
function d(string $a = '') {}
