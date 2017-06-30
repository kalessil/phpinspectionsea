<?php

    if ($a || <error descr="Confusing conditions structure: please wrap needed with '(...)'.">$b && $c</error>) {}
    if (<error descr="Confusing conditions structure: please wrap needed with '(...)'.">$a && $b</error> || $c) {}

    if ($a || ($b && $c)) {}
    if ($a && ($b || $c)) {}
