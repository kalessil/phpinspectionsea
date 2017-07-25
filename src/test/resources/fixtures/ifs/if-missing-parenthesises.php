<?php

    if ($a || <error descr="Operations priority might differ from what you expect: please wrap needed with '(...)'.">$b && $c</error>) {}
    if (<error descr="Operations priority might differ from what you expect: please wrap needed with '(...)'.">$a && $b</error> || $c) {}

    if ($a || ($b && $c)) {}
    if ($a && ($b || $c)) {}

    if ($a = <error descr="Operations priority might differ from what you expect: please wrap needed with '(...)'.">function1() && $b</error>)                     {}
    if ($a = <error descr="Operations priority might differ from what you expect: please wrap needed with '(...)'.">function1() && $b = function2()</error>)       {}
    if ($a = <error descr="Operations priority might differ from what you expect: please wrap needed with '(...)'.">function1() && $b && $c = function2()</error>) {}
    if ($a = <error descr="Operations priority might differ from what you expect: please wrap needed with '(...)'.">function1() && $b = function2() && $b</error>) {}
