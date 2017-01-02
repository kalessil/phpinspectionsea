<?php

    /* direct and inverted patterns */
    echo isset($x[0]) ? <weak_warning descr="' ... ?? ...' construction should be used instead.">$x[0]</weak_warning> : null;
    echo !isset($x[0]) ? null : <weak_warning descr="' ... ?? ...' construction should be used instead.">$x[0]</weak_warning>;

    /* alternative pattern */
    echo array_key_exists(0, $x) ? <weak_warning descr="' ... ?? ...' construction should be used instead.">$x[0]</weak_warning> : null;
    echo !array_key_exists(0, $x) ? null : <weak_warning descr="' ... ?? ...' construction should be used instead.">$x[0]</weak_warning>;

    /* false-positives */
    echo array_key_exists(0, $x) ? $x[0] : 'default';
    echo isset($x[0], $s[1]) ? $x[0] : 'default';
    echo isset($x[0]) ? $x[0]->x : 'default';
    echo isset($x[0], $x[0]) ? $x[0] : 'default';