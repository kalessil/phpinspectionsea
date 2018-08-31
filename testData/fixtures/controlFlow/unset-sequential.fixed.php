<?php

function cases_holder() {
    unset($x, $y, $z);
    /** dock-block should not break inspection */
    /** multiple dock-blocks should not break inspection */

    $x = $y = $z;
    unset($x, $y, $z);
}