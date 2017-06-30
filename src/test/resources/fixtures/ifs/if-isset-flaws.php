<?php

    if (
        <weak_warning descr="It seems like 'null === $a' is already covered by 'isset(...)'.">null === $a</weak_warning> ||
        <weak_warning descr="It seems like '$b !== null' is already covered by 'isset(...)'.">$b !== null</weak_warning> ||
        isset($a, $b)
    ) {}

    if (null === $a || $b !== null || isset($c)) {}