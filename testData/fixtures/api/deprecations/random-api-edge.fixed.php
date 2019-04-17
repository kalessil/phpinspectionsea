<?php

namespace {
    use function mt_rand;

    mt_srand();
    mt_getrandmax();
    mt_rand(1);
    random_int(1, 2);
    random_int(1, 2);

    /* false-positives */
    mt_rand(1);
}