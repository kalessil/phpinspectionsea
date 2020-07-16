<?php

return [
    $a && <error descr="[EA] '$a' seems to be always true when reached (the operation is incorrect or can be simplified).">$a</error>,
    $a && $b && <error descr="[EA] '$a' seems to be always true when reached (the operation is incorrect or can be simplified).">$a</error>,
    ($a && $b) && (<error descr="[EA] '$a' seems to be always true when reached (the operation is incorrect or can be simplified).">$a</error> && $c),
    $a || <error descr="[EA] '$a' seems to be always false when reached (the operation is incorrect or can be simplified).">$a</error>,
    $a || $b || <error descr="[EA] '$a' seems to be always false when reached (the operation is incorrect or can be simplified).">$a</error>,
    ($a || $b) || (<error descr="[EA] '$a' seems to be always false when reached (the operation is incorrect or can be simplified).">$a</error> || $c),

    /* false-positives */
    $a || $b,
    $a || $b || $c,
    ($a || $b) && ($a && $c),
];
