<?php

    /* patterns, incl. un-escaping */
    echo 'string';
    echo '$"';
    echo '';

    /* false-positives */
    echo '123';
    echo "{$a}";
    echo "'\'";
    echo "'%s'";
    echo "\n\r\t\v\e\f\000\x00\u{00}\\";

    $a = <<<'NOWDOC'
test
NOWDOC;
