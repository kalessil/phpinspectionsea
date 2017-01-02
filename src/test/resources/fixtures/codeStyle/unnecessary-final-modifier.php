<?php

class NonFinalClass
{
    final public function x()
    {
    }
}

final class FinalClass
{
    final public function <weak_warning descr="Unnecessary final modifier (class is already final).">x</weak_warning> ()
    {
    }
}