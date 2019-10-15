<?php

    <error descr="[EA] Probably '+=' operator should be used here (or proper formatting applied).">$x =+ $y</error>;
    <error descr="[EA] Probably '-=' operator should be used here (or proper formatting applied).">$y =- $y</error>;
    <error descr="[EA] Probably '!=' operator should be used here (or proper formatting applied).">$z =! $y</error>;

    /* false-positives */
    $x=-$y;
    $y = -$y;
    $z -= $y;
