<?php

class ClassWithIsoConstant { const ISO8601 = ''; }

function cases_holder()
{
    return [
        DATE_ATOM,
        DateTime::ATOM,

        __DATE_ISO8601,
        ClassWithIsoConstant::ISO8601,
    ];
}