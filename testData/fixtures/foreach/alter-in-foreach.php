<?php

function cases_holder() {
    $source = array();

    foreach ($source as <error descr="Provokes a PHP Fatal error (key element cannot be a reference).">&</error> $id => $element) {
        <weak_warning descr="Can be refactored as '$element = ...' if $element is defined as a reference (ensure that array supplied). Suppress if causes memory mismatches.">$source[$id]</weak_warning>
            = $element + 1;
    }
    unset(
        <weak_warning descr="Unsetting $element is not needed because it's not a reference.">$element</weak_warning>,
        $id
    );

    foreach ($source as $i1 => &<warning descr="This variable must be unset just after foreach to prevent possible side-effects.">$level1</warning>) {
        foreach ($source as $i2 => $level2) {
            foreach ($source as $i3 => $level3) {
                echo $level1.$level2.$level3;
            }
        }
    }
    unset(
        <weak_warning descr="Unsetting $level2 is not needed because it's not a reference.">$level2</weak_warning>,
        <weak_warning descr="Unsetting $level3 is not needed because it's not a reference.">$level3</weak_warning>
    );

    foreach ($source as & $el) {
        ++$el;
    }
}