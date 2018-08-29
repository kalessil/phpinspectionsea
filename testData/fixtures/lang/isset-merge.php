<?php

function cases_holder() {
    return [
        isset($a) && <weak_warning descr="This can be merged into the previous 'isset(..., ...[, ...])'.">isset($b)</weak_warning>,
        isset($a) && (<weak_warning descr="This can be merged into the previous 'isset(..., ...[, ...])'.">isset($b)</weak_warning> && isset($c)),
        isset($a) && <weak_warning descr="This can be merged into the previous 'isset(..., ...[, ...])'.">isset($b)</weak_warning> && ($c || $d),

        isset($a) && !isset($b),

        !isset($a) || !<weak_warning descr="This can be merged into the previous '!isset(..., ...[, ...])'.">isset($b)</weak_warning>,
        !isset($a) || (!<weak_warning descr="This can be merged into the previous '!isset(..., ...[, ...])'.">isset($b)</weak_warning> || !isset($c)),

        isset($a) || !isset($b),
    ];
}