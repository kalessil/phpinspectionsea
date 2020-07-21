<?php

return [
    $a && <error descr="[EA] '$a' seems to be always true when reached (the operation is incorrect or can be simplified).">$a</error>,
    $a && $b && <error descr="[EA] '$a' seems to be always true when reached (the operation is incorrect or can be simplified).">$a</error>,
    ($a && $b) && (<error descr="[EA] '$a' seems to be always true when reached (the operation is incorrect or can be simplified).">$a</error> && $c),
    $a || <error descr="[EA] '$a' seems to be always false when reached (the operation is incorrect or can be simplified).">$a</error>,
    $a || $b || <error descr="[EA] '$a' seems to be always false when reached (the operation is incorrect or can be simplified).">$a</error>,
    ($a || $b) || (<error descr="[EA] '$a' seems to be always false when reached (the operation is incorrect or can be simplified).">$a</error> || $c),

    /* false-positives: generic cases */
    $a || $b,
    $a || $b || $c,
    ($a || $b) && ($a && $c),
    /* false-positives: conflicting inspections */
    !is_dir($directory) && mkdir($directory, 0777, true) && !is_dir($directory),
    is_dir($directory) || mkdir($directory, 0777, true) || is_dir($directory),
];
