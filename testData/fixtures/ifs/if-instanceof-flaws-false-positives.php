<?php

    class myDateTime extends \DateTime {}

    if (
        <warning descr="This condition is ambiguous and can be safely removed.">$x instanceof \myDateTime</warning> &&
        $x instanceof \DateTime &&
        $x instanceof \DateTimeInterface
    ) {}

    if ($x instanceof \myDateTime && $x instanceof \DateTimeInterface) {}