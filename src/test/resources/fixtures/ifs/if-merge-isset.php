<?php

    if (isset($a) && <warning descr="This can be merged into the previous 'isset(..., ...[, ...])'.">isset($b)</warning>)    {}
    if (!isset($a) || !<warning descr="This can be merged into the previous '!isset(..., ...[, ...])'.">isset($b)</warning>) {}

    if (isset($a) || isset($b))   {}
    if (!isset($a) && !isset($b)) {}