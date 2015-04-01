<?php

function noUnsetNeeded($id) {
    foreach ($id as &$i) {
        $i = str_replace('x', 'y', $i);
    }
    // clear the scope
    //unset($i);

    return $id;
}