<?php

$num_names = 5;
$names = array('', '', '', '', '');
for ($i = 0; $i < $num_names; ++$i) {
    if ($names[$i] === null) {
        /** shall warn here */
        for ($i = 0; $i < $num_names; ++$i) { // <- reported
            unset($names[$i]);
        }
        $num_names = 0;

        /** shall warn here */
        foreach ($names as $i => $v) { // <- reported
            unset($names[$i]);
        }

    }
}

foreach ($names as $i => $vv) {
    if ($vv === null) {
        /** shall warn here */
        for ($i = 0, $vv = ''; $i < $num_names; ++$i) { // <- reported
            unset($names[$i]);
        }
        $num_names = 0;

        /** shall warn here */
        foreach ($names as $i => $v) { // <- reported
            unset($names[$i]);
        }

    }
}

for ($i = 0; $i > 0, $i < 255; ++$i) { // <- reported
    echo $i;
}

function suspiciousLoop($i)
{
    for ($i = 0; $i < 255; ++$i) { // <- reported
        echo $i;
    }
}