<?php

    if (<warning descr="[EA] Non-boolean if expression. (got Number)">1</warning>) {}

    if (<warning descr="[EA] Non-boolean if expression. (got Number)">1.0</warning>) {}

    if (<warning descr="[EA] Non-boolean if expression. (got Array creation expression)">[]</warning>) {}

    if (<warning descr="[EA] Non-boolean if expression. (got New expression)">new stdClass()</warning>) {}

    if (<warning descr="[EA] Non-boolean if expression. (got Number)">1</warning> && <warning descr="[EA] Non-boolean if expression. (got Number)">0</warning>) {}

    if (true) {}
    if (false) {}

    $x=true; if ($x) {}
    $x=1; if (<warning descr="[EA] Non-boolean if expression. (got int)">$x</warning>) {}
