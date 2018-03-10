<?php

return [
    0 === <warning descr="'strncmp('', 'what', 4)' would be a solution not depending on the string length.">strpos('', 'what')</warning>,
    <warning descr="'strncmp('', 'what', 4)' would be a solution not depending on the string length.">strpos('', 'what')</warning> === 0,
    <warning descr="'strncmp('', 'what', 4)' would be a solution not depending on the string length.">strpos('', 'what')</warning> !== 0,

    0 === <warning descr="'strncasecmp('', 'what', 4)' would be a solution not depending on the string length.">stripos('', 'what')</warning>,
    <warning descr="'strncasecmp('', 'what', 4)' would be a solution not depending on the string length.">stripos('', 'what')</warning> === 0,
    <warning descr="'strncasecmp('', 'what', 4)' would be a solution not depending on the string length.">stripos('', 'what')</warning> !== 0,

    /* false-positives: not targeted contexts */
    0 === strpos('', $what),
    0 === strpos('', 'what', 1),

    /* false-positives: injections */
    strpos('', "$what") !== 0,
];