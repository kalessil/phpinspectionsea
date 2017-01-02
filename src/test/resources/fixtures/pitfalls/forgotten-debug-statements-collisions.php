<?php

class DebugClass1
{
    static public function debug()
    {
    }
}

class DebugClass2
{
    static public function debug()
    {
    }
}

DebugClass1::<error descr="Please ensure this is not a forgotten debug statement.">debug</error>();
DebugClass2::<error descr="Please ensure this is not a forgotten debug statement.">debug</error>();