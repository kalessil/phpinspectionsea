<?php

function suppression_cases_holder(array $source, array $destination) {
    /* the very first suppression suppose to disable reporting all statements */

    foreach ($source as $item) {
        /** @noinspection OnlyWritesOnParameterInspection */
        $destination['...'][] = '...';
        $destination['...'][] = '...';
    }

    if (!empty($source)) {
        $destination['...'][] = '...';
        $destination['...'][] = '...';
    }

    $destination['...'][] = '...';
    $destination['...'][] = '...';

    switch ($source) {
        case 'source' :
            $destination['...'][] = '...';
            $destination['...'][] = '...';
            break;
    }
}