<?php

class NonFinalClass
{
    final public function x() {}
    final private function <weak_warning descr="Unnecessary final modifier.">y</weak_warning>() {}
}

final class FinalClass
{
    final public function <weak_warning descr="Unnecessary final modifier.">x</weak_warning>() {}
}