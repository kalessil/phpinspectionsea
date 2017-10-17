<?php

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

    /* false-positives: mixed out operators */
    if ($a || $b) { if ($c) { } }