<?php

    /* pattern: values corruption might take place */
    echo <error descr="[EA] This may not work as expected (wrap condition into '()' to specify intention).">$a & $b</error> ? 0 : 1;
    echo <error descr="[EA] This may not work as expected (wrap condition into '()' to specify intention).">$a | $b</error> ? 0 : 1;
    echo <error descr="[EA] This may not work as expected (wrap condition into '()' to specify intention).">$a - $b</error> ? 0 : 1;
    echo <error descr="[EA] This may not work as expected (wrap condition into '()' to specify intention).">$a + $b</error> ? 0 : 1;
    echo <error descr="[EA] This may not work as expected (wrap condition into '()' to specify intention).">$a / $b</error> ? 0 : 1;
    echo <error descr="[EA] This may not work as expected (wrap condition into '()' to specify intention).">$a * $b</error> ? 0 : 1;
    echo <error descr="[EA] This may not work as expected (wrap condition into '()' to specify intention).">$a % $b</error> ? 0 : 1;
    echo <error descr="[EA] This may not work as expected (wrap condition into '()' to specify intention).">$a ^ $b</error> ? 0 : 1;

    /* literal operators might behave differently in some cases */
    echo <error descr="[EA] This may not work as expected (wrap condition into '()' to specify intention).">$a and $b ? 0 : 1</error>;
    echo <error descr="[EA] This may not work as expected (wrap condition into '()' to specify intention).">$a or $b ? 0 : 1</error>;

    /* pattern: bugs */
    echo (<error descr="[EA] True and false variants are identical, most probably this is a bug.">$b ? 0 : 0</error>);


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
    echo $a <=> $b ? 0 : 1;
