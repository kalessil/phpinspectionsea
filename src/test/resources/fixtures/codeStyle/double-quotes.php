<?php

    /* patterns, incl. un-escaping */
    echo <weak_warning descr="Safely use single quotes instead.">"string"</weak_warning>;
    echo <weak_warning descr="Safely use single quotes instead.">"\$\""</weak_warning>;
    echo <weak_warning descr="Safely use single quotes instead.">""</weak_warning>;

    /* false-positives */
    echo '123';
    echo "{$a}";
    echo "'\'";
    echo "'%s'";
    echo "\n\r\t\v\e\f\000\x00\u{00}\\";

    $a = <<<'NOWDOC'
test
NOWDOC;
