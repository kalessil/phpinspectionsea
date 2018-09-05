<?php

class NonFinalClass
{
    final public function x() {}
    private function y() {}

    final private function __construct() {}
    final private function __destruct() {}
}

final class FinalClass
{
    public function x() {}
    private function __construct() {}
}