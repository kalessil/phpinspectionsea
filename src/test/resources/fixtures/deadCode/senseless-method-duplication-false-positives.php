<?php

namespace SMD\One;

const SMD_CONST = 1;
function smd_func() {}

class SMDOneClass {
    private function f() {
        echo SMD_CONST.smd_func();
    }
}


namespace SMD\Two;

const SMD_CONST = 1;
function smd_func() {}

class SMDTwoClass extends \SMD\One\SMDOneClass
{
    private function f() {
        echo SMD_CONST.smd_func();
    }
}
