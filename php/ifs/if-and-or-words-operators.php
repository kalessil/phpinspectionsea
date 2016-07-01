<?php

    $x = 0;
    if (
        ($x > 0 and $x < 10)   // <-- reported
        or                     // <-- reported
        ($x > 20 and $x < 30)  // <-- reported
    ) {
        return;
    }