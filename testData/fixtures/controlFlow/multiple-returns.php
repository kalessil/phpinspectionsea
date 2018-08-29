<?php

/* traits are ignored as of initial inspection version */
trait A
{
    public function test() {
        if ($this instanceof \stdClass) {
            return false;
        }
        return true;
    }
}

class Test_A
{
    public function <warning descr="Method has 2 return points, try to introduce just one to uncover complexity behind.">TwoReturnsMethod</warning>() {
        if ($this instanceof TestA) {
            return false;
        }
        return true;
    }

    public function <error descr="Method has 4 return points, try to introduce just one to uncover complexity behind.">FourReturnsMethod</error>($a, $b, $c) {
        if ($this instanceof $a) {
            return false;
        }
        if ($this instanceof $b) {
            return false;
        }
        if ($this instanceof $c) {
            return false;
        }
        return true;
    }
}

class Test_B
{
    public function customMethod() {
        return true;
    }

    private function myFunction() {
        return function () {
            return '';
        };
    }

    public function factory() {
        return new class
        {
            public function calculate() {
                return 0;
            }

            public function <warning descr="Method has 2 return points, try to introduce just one to uncover complexity behind.">isValid</warning>() {
                if ($this instanceof Test_Valid) {
                    return true;
                }
                return false;
            }
        };
    }
}  