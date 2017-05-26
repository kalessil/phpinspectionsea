<?php

    $stdClass = new stdClass();
    if (
        <weak_warning descr="Probable bug: ensure this behaves properly with 'instanceof(...)' in this scenario.">$stdClass === null</weak_warning> &&
        <weak_warning descr="Probable bug: ensure this behaves properly with 'instanceof(...)' in this scenario.">null !== $stdClass</weak_warning> &&
        $stdClass instanceof stdClass
    ) {}

    class myDateTime extends DateTime {}
    $dateTime = new myDateTime();
    if (
        $dateTime instanceof DateTimeInterface ||
        <warning descr="This condition is ambiguous and can be safely removed.">$dateTime instanceof DateTime</warning> ||
        <warning descr="This condition is ambiguous and can be safely removed.">$dateTime instanceof \myDateTime</warning>
    ) {}