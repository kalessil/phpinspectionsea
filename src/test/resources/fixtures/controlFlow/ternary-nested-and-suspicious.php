<?php

    /* Pattern: values corruption might take place */
    echo <weak_warning descr="This may not work as expected (wrap condition into '()' to specify intention).">$a & $b</weak_warning> ? 0 : 1;
    echo <weak_warning descr="This may not work as expected (wrap condition into '()' to specify intention).">$a | $b</weak_warning> ? 0 : 1;
    echo <weak_warning descr="This may not work as expected (wrap condition into '()' to specify intention).">$a - $b</weak_warning> ? 0 : 1;
    echo <weak_warning descr="This may not work as expected (wrap condition into '()' to specify intention).">$a + $b</weak_warning> ? 0 : 1;
    echo <weak_warning descr="This may not work as expected (wrap condition into '()' to specify intention).">$a / $b</weak_warning> ? 0 : 1;
    echo <weak_warning descr="This may not work as expected (wrap condition into '()' to specify intention).">$a * $b</weak_warning> ? 0 : 1;
    echo <weak_warning descr="This may not work as expected (wrap condition into '()' to specify intention).">$a % $b</weak_warning> ? 0 : 1;
    echo <weak_warning descr="This may not work as expected (wrap condition into '()' to specify intention).">$a ^ $b</weak_warning> ? 0 : 1;

    /* literal operators might behave differently in some cases */
    echo <error descr="This may not work as expected (wrap condition into '()' to specify intention).">$a and $b ? 0 : 1</error>;
    echo <error descr="This may not work as expected (wrap condition into '()' to specify intention).">$a or $b ? 0 : 1</error>;

    /* Pattern: bugs */
    echo (<error descr="True and false variants are identical, most probably this is a bug.">$b ? 0 : 0</error>);

    /* Pattern: nested ternaries */
    echo $a ? $a : (<weak_warning descr="Nested ternary operator should not be used (maintainability issues).">$b ? $b : null</weak_warning>);
    echo !$a ? (<weak_warning descr="Nested ternary operator should not be used (maintainability issues).">$b ? $b : null</weak_warning>) : $a;
    echo (<weak_warning descr="Nested ternary operator should not be used (maintainability issues).">$b ? $b : null</weak_warning>) ? !$a : $a;


    /* false-positives: condition is specified well */
    echo ($a + $b) ? 0 : 1;
    /* false-positives: conditions intentions are clear */
    echo $a > $b ? 0 : 1;
    echo $a >= $b ? 0 : 1;
    echo $a < $b ? 0 : 1;
    echo $a <= $b ? 0 : 1;
    echo $a && $b ? 0 : 1;
    echo $a || $b ? 0 : 1;
    echo $a == $b ? 0 : 1;
    echo $a != $b ? 0 : 1;
    echo $a === $b ? 0 : 1;
    echo $a !== $b ? 0 : 1;
    echo $a instanceof stdClass ? 0 : 1;



