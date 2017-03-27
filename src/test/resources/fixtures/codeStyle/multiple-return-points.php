<?php

trait A
{
    public function test()
    {
        if ($this instanceof \stdClass) {
            return false;
        }
        return true;
    }
}

class Test_A
{
    public function <warning descr="Method with multiple return points.">customMethod</warning>()
    {

        if ($this instanceof TestA) {
            return false;
        }

        return true;
    }
}

class Test_B
{
    public function customMethod()
    {
        return true;
    }

    public function factory()
    {

        return new class
        {

            public function calculate()
            {
                return 0;
            }

            public function <warning descr="Method with multiple return points.">isValid</warning>()
            {
                if ($this instanceof Test_Valid) {
                    return true;
                }
                return false;
            }
        };
    }
}  