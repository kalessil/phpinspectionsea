<?php

    /* logical operands and multi-value cases */
    if ($x == 5 && <error descr="'$x == 5 && $x == 6' seems to be always false.">$x == 6</error>) {}
    if ($x == 5 && <error descr="'$x == 5 && $x === 6' seems to be always false.">$x === 6</error>) {}
    if ($x === 5 && <error descr="'$x === 5 && $x === 6' seems to be always false.">$x === 6</error>) {}
    if ($x != 5 || <error descr="'$x != 5 || $x != 6' seems to be always true.">$x != 6</error>) {}
    if ($x != 5 || <error descr="'$x != 5 || $x !== 6' seems to be always true.">$x !== 6</error>) {}
    if ($x !== 5 || <error descr="'$x !== 5 || $x !== 6' seems to be always true.">$x !== 6</error>) {}
    if (<error descr="'$x == 5' seems to have no effect due to '$x != 6'.">$x == 5</error> || $x != 6) {}
    if ($x == 5 && <error descr="'$x != 6' seems to have no effect due to '$x == 5'.">$x != 6</error>) {}

    /* logical operands and multi-value edge-cases */
    if ($x == 5 && <error descr="'$x == 5' seems to have no effect due to '$x == 5'.">$x == 5</error>) {}
    if ($x != 5 || <error descr="'$x != 5' seems to have no effect due to '$x != 5'.">$x != 5</error>) {}
    if ($x == 5 || <error descr="'$x == 5 || $x != 5' seems to be always true.">$x != 5</error>) {}
    if ($x == 5 && <error descr="'$x == 5 && $x != 5' seems to be always false.">$x != 5</error>) {}

    if ($x == $y && <error descr="'$y == $x' seems to have no effect due to '$x == $y'.">$y == $x</error>) {}
    if ($x != $y || <error descr="'$y != $x' seems to have no effect due to '$x != $y'.">$y != $x</error>) {}
    if ($x == $y || <error descr="'$x == $y || $y != $x' seems to be always true.">$y != $x</error>) {}
    if ($x == $y && <error descr="'$x == $y && $y != $x' seems to be always false.">$y != $x</error>) {}

    /* false-positives: non-constant values */
    if ($x == 5 && $x == $y) {}
    if ($x == $z && $x == $y) {}

    /* logical operands and multi-value cases for if-statements */
    if ($x === 5) { return 6 == $x; } // always false
    if ($x === 5) { return 6 === $x; } // always false
    if ($x === 5) { return 6 != $x; } // always true
    if ($x === 5) { return 6 !== $x; } // always true

    /* logical operands and multi-value cases for if-statements - edge-cases */
    if ($x === 5 && $whatever) { return 5 === $x; } // always true
    if ($x === 5 && $whatever) { return 5 !== $x; } // always false
    if ($x === $y) { return $y === $x; } // always true
    if ($x === $y) { return $y !== $x; } // always false

    /* false-positives: assignments, non-constant values */
    if ($x === 5) { return $x == $y; }
    if ($x == $z) { return $x == $y; }
    if ($x === 5) { $x = '...'; return $x === 6; }