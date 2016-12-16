<?php

    $source = [];
    $result = [];
    foreach ([] as $v) {
        /* basic patterns */
        $result[$v->toString()] = <warning descr="Duplicated method calls should be moved to local variable">$v->toString()</warning>;
        $result[trim($v)]       = <warning descr="Duplicated method calls should be moved to local variable">trim($v)</warning>;
        $result[(trim($v))]     = <warning descr="Duplicated method calls should be moved to local variable">trim($v)</warning>;

        /* sophisticated patterns */
        $result['prefix_' . trim($v)] = <warning descr="Duplicated method calls should be moved to local variable">trim($v)</warning>;
        $result['source_' . trim($v)] = $source[<warning descr="Duplicated method calls should be moved to local variable">trim($v)</warning>];

        /* false-positives */
        $result[trim($v)] = 'string';
        $result['string'] = trim($v);
    }