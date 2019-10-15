<?php

class NonFinalClass
{
    final public function x() {}
    final private function <weak_warning descr="[EA] Unnecessary final modifier.">y</weak_warning>() {}

    final private function __construct() {}
    final private function __destruct() {}
}

final class FinalClass
{
    final public function <weak_warning descr="[EA] Unnecessary final modifier.">x</weak_warning>() {}
    final private function <weak_warning descr="[EA] Unnecessary final modifier.">__construct</weak_warning>() {}
}