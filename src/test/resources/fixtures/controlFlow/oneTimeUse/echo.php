<?php

function cases_holder() {
    <warning descr="Variable $one is redundant.">$one</warning> = '...';
    echo $one;

    $two = '...';
    echo $two . '...';
}