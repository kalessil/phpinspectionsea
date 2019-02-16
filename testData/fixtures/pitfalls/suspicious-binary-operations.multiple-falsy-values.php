<?php

function cases_holder() {
    if ($path == '' && $path == 0) {}     // always true
    if ($path == '' && $path == null) {}  // always true
    if ($path == '' && $path == false) {} // always true
    if ($path == '' && $path == []) {}    // always true

    if ($path == '' && $path != 0) {}     // always false
    if ($path == '' && $path != null) {}  // always false
    if ($path == '' && $path != false) {} // always false
    if ($path == '' && $path != []) {}    // always false

    if ($path != '' && $path != 0) {}     // always true
    if ($path != '' && $path != null) {}  // always true
    if ($path != '' && $path != false) {} // always true
    if ($path != '' && $path != []) {}    // always true

    if ($path != '' && $path == 0) {}     // always false
    if ($path != '' && $path == null) {}  // always false
    if ($path != '' && $path == false) {} // always false
    if ($path != '' && $path == false) {} // always false
}