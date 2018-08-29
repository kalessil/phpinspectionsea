<?php

class ClassWithIsoConstant { const ISO8601 = ''; }

function cases_holder()
{
    return [
        <error descr="The format is not compatible with ISO-8601. Use DATE_ATOM for compatibility with ISO-8601 instead.">DATE_ISO8601</error>,
        <error descr="The format is not compatible with ISO-8601. Use DateTime::ATOM for compatibility with ISO-8601 instead.">DateTime::ISO8601</error>,

        __DATE_ISO8601,
        ClassWithIsoConstant::ISO8601,
    ];
}