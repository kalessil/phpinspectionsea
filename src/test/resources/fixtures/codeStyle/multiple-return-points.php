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

    private function myFunction()
    {
        return function () {
            return '';
        };
    }

    public function <warning descr="Method with multiple return points.">calculate</warning>($a, $b)
    {
        if ($a > $b) {
            return $a + $b;
        }
        return $a - $b;
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