<?php

function cases_holder() {
    if ($a) {
       <weak_warning descr="If statement can be merged into parent.">if</weak_warning> ($b) {
       }
    }

    if ($a)
       <weak_warning descr="If statement can be merged into parent.">if</weak_warning> ($b) {
       }

    if ($a && $b) {
       <weak_warning descr="If statement can be merged into parent.">if</weak_warning> ($c) {
       }
    }

    if ($a || $b) {
       <weak_warning descr="If statement can be merged into parent.">if</weak_warning> ($c || $d) {
       }
    }

    /* false-positives: mixed out operators */
    if ($a || $b) { if ($c) { } }
}