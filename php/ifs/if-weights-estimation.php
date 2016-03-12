<?php

    if (isset($x) && $x) {
        unset($x);
    }

    if (empty($x) && $x) {
        unset($x);
    }

    if (isset($x[uniqid()]) && $x[uniqid()]) {
        unset($x);
    }

    if (isset($x[uniqid()]) && $x) {
        unset($x);
    }