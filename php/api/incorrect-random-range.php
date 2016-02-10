<?php

    rand(2, 1);       // <- reported
    rand(1, 2);

    mt_rand(2, 1);    // <- reported
    mt_rand(1, 2);

    random_int(2, 1); // <- reported
    random_int(1, 2);