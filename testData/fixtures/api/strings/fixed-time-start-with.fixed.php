<?php

return [
    0 === strncmp('', 'what', 4),
    strncmp('', 'what', 4) === 0,
    strncmp('', 'what', 4) !== 0,

    0 === strncasecmp('', 'what', 4),
    strncasecmp('', 'what', 4) === 0,
    strncasecmp('', 'what', 4) !== 0,

    /* false-positives: not targeted contexts */
    0 === strpos('', $what),
    0 === strpos('', 'what', 1),

    /* false-positives: injections */
    strpos('', "$what") !== 0,
];