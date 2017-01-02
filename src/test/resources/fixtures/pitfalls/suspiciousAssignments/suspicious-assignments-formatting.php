<?php

    <error descr="Probably '+=' operator should be used here (or proper formatting applied).">$x =+ $y</error>;
    <error descr="Probably '-=' operator should be used here (or proper formatting applied).">$x =- $y</error>;
    <error descr="Probably '!=' operator should be used here (or proper formatting applied).">$x =! $y</error>;

    /* false-positives */
    $x=-$y;
    $x = -$y;
    $x -= $y;
