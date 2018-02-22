<?php

    <warning descr="'srand(...)' has recommended replacement 'mt_srand(...)', consider migrating.">srand()</warning>;
    <warning descr="'getrandmax(...)' has recommended replacement 'mt_getrandmax(...)', consider migrating.">getrandmax()</warning>;
    <warning descr="'rand(...)' has recommended replacement 'mt_rand(...)', consider migrating.">rand(1)</warning>;
    <warning descr="'rand(...)' has recommended replacement 'random_int(...)', consider migrating.">rand(1, 2)</warning>;
    <warning descr="'mt_rand(...)' has recommended replacement 'random_int(...)', consider migrating.">mt_rand(1, 2)</warning>;

    /* false-positives */
    mt_rand(1);