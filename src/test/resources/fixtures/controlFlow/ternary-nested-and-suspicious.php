<?php

    /* pattern: nested ternaries */
    echo $a ? $a : (<warning descr="Nested ternary operator should not be used (maintainability issues).">$b ? $b : null</warning>);
    echo !$a ? (<warning descr="Nested ternary operator should not be used (maintainability issues).">$b ? $b : null</warning>) : $a;
    echo (<warning descr="Nested ternary operator should not be used (maintainability issues).">$b ? $b : null</warning>) ? !$a : $a;

    /* false-positives: sequential elvis operators */
    $x = $a ?: $b ?: $c;
