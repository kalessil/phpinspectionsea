<?php

/* pre-defined $_ vars weights calculation: add 0 instead of 1 */
if ($_SERVER ['REQUEST_METHOD'] === 'POST' && array_key_exists('comment', $_POST)) {
    echo $_POST['comment'];
}

/* interconnected statements: array and array access */
$in = [];
if (is_array_indexed($in) && is_array($in[0])) {
    echo $in[0];
}

/* interconnected statements: assigned variable case */
if (($count = $counter->count()) && $count > 0 && $count < 255) {
    echo $count;
}