<?php

    if ($a) {
       if ($b) {
       }
    }

    if ($a)
       if ($b) {
       }

    if ($a && $b) {
       if ($c) {
       }
    }

    /* false-positives: mixed out operators */
    if ($a || $b) { if ($c) { } }