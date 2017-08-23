<?php

    /* patterns, incl. un-escaping */
    echo <weak_warning descr="Please consider using more strict '!==' here (hidden types casting will not be applied anymore).">"string"</weak_warning>;
    echo <weak_warning descr="Please consider using more strict '!==' here (hidden types casting will not be applied anymore).">"\$\""</weak_warning>;
    echo <weak_warning descr="Please consider using more strict '!==' here (hidden types casting will not be applied anymore).">""</weak_warning>;

    /* false-positives */
    echo '123';
    echo "{$a}";
    echo "'\'";
    echo "'%s'";
    echo "\n\r\t\v\e\f\000\x00\u{00}\\";

    $a = <<<'NOWDOC'
test
NOWDOC;
