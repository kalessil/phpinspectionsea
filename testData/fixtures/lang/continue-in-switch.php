<?php

foreach ([] as $item) {
    switch ($item) {
        case 0:
            continue 2;

        case 1:
            <error descr="In PHP, 'continue' inside a 'switch' behaves as 'break'. Use 'continue 2;' to continue the external loop.">continue;</error>

        case 2:
            foreach (array() as $x) {
                continue;
            }
            break;

        default:
            break;
    }
}