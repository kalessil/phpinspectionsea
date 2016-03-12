<?php

    $x = [];
    if (!$x && is_array($x)) {
        echo 'if';
    } elseif (!$x && is_array($x)) {
        echo 'elseif';
    } else {
        echo 'else';
    }