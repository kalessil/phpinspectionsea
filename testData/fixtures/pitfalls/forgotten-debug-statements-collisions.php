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

<error descr="Please ensure this is not a forgotten debug statement.">DebugClass1::debug()</error>;
<error descr="Please ensure this is not a forgotten debug statement.">DebugClass2::debug()</error>;