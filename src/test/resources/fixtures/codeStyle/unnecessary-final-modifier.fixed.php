<?php

class NonFinalClass
{
    final public function x() {}
    private function y() {}
}

final class FinalClass
{
    public function x() {}
}