<?php

namespace {
    use function mt_rand;

    <warning descr="[EA] 'srand(...)' has recommended replacement 'mt_srand(...)', consider migrating.">srand()</warning>;
    <warning descr="[EA] 'getrandmax(...)' has recommended replacement 'mt_getrandmax(...)', consider migrating.">getrandmax()</warning>;
    <warning descr="[EA] 'rand(...)' has recommended replacement 'mt_rand(...)', consider migrating.">rand(1)</warning>;
    <warning descr="[EA] 'rand(...)' has recommended replacement 'random_int(...)', consider migrating.">rand(1, 2)</warning>;
    <warning descr="[EA] 'mt_rand(...)' has recommended replacement 'random_int(...)', consider migrating.">mt_rand(1, 2)</warning>;

    /* false-positives */
    mt_rand(1);
}