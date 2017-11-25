<?php

/* [mb_]strlen($path) can be dropped */
substr($path, 0, <warning descr="'-2' can be used instead.">strlen($path) - 2</warning>);
mb_substr($path, 0, <warning descr="'-2' can be used instead.">mb_strlen($path) - 2</warning>);
mb_substr($path, 0, <warning descr="'-2' can be used instead.">mb_strlen($path) - 2</warning>, 'encoding');

/* false-positives: over-complication, e.g. -(strlen($pathPrefix) - 1) */
substr($path, 0, strlen($path) - strlen($pathPrefix));

/* 3rd argument can be dropped completely */
substr(
    $path,
    strlen($pathPrefix),
    <warning descr="'strlen($path) - strlen($pathPrefix)' can be safely dropped.">strlen($path) - strlen($pathPrefix)</warning>
);
mb_substr(
    $path, mb_strlen($pathPrefix),
    <warning descr="'mb_strlen($path) - mb_strlen($pathPrefix)' can be safely dropped.">mb_strlen($path) - mb_strlen($pathPrefix)</warning>
);
mb_substr(
    $path, mb_strlen($pathPrefix),
    <warning descr="'mb_strlen($path) - mb_strlen($pathPrefix)' can be safely dropped.">mb_strlen($path) - mb_strlen($pathPrefix)</warning>,
    'encoding'
);
