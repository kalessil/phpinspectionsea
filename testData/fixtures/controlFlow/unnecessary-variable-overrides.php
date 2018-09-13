<?php

function cases_holder() {
    <weak_warning descr="Unnecessary assignment, a nested call would simplify workflow.">$a</weak_warning> = '';
    $a = trim($a);

    <weak_warning descr="Unnecessary assignment, a nested call would simplify workflow.">$b</weak_warning> = '';
    $b = strtolower(trim($b));

    /* false-positive: parameter by reference */
    $c = [];
    $c = array_pop($c);

    /* false-positive: 2 and more parameters */
    $d = '';
    $d = str_replace('search', 'replace', $d);
}