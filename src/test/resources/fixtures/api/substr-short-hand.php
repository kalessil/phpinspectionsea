<?php

/* [mb_]strlen($path) can be dropped */
substr(
    $path,
    0,
    <warning descr="Normally 'strlen($path)' can be dropped, so '-strlen($pathPrefix)' is only left (range bugs can popup, see a bug-report #271 on Bitbucket).">strlen($path)</warning> - strlen($pathPrefix));
mb_substr(
    $path,
    0,
    <warning descr="Normally 'mb_strlen($path)' can be dropped, so '-mb_strlen($pathPrefix)' is only left (range bugs can popup, see a bug-report #271 on Bitbucket).">mb_strlen($path)</warning> - mb_strlen($pathPrefix));
mb_substr(
    $path,
    0,
    <warning descr="Normally 'mb_strlen($path)' can be dropped, so '-mb_strlen($pathPrefix)' is only left (range bugs can popup, see a bug-report #271 on Bitbucket).">mb_strlen($path)</warning> - mb_strlen($pathPrefix), 'encoding');

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
