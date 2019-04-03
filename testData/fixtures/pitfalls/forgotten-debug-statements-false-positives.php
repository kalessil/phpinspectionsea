<?php

use function var_dump;

/* false-positives: core debug function inside known wrappers */
function dd($x) {
    var_dump($x);
}

echo var_export($a, true);
echo print_r($a, true);

ob_start();
print_r($a);

ob_start();
@print_r($a);

