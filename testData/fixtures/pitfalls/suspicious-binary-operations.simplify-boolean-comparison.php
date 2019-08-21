<?php

function cases_holder() {
    if (($x && $y) || (!$x && !$y)) {}
    if (($x && !$y) || (!$x && $y)) {}

    if (($x === true && $y) || ($x === false && !$y)) {}
    if (($x !== false && $y) || ($x !== true && !$y)) {}
}