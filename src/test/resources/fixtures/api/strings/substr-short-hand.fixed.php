<?php

/* [mb_]strlen($path) can be dropped */
substr($path, 0, -2);
mb_substr($path, 0, -2);
mb_substr($path, 0, -2, 'encoding');

/* correct length calculation */
substr($path, 1, -1);
substr($path, 1);

/* false-positives: non-constant start, over-complication, e.g. -(strlen($pathPrefix) - 1) */
substr($path, $variable, strlen($path) - 2);
substr($path, 0, strlen($path) - strlen($pathPrefix));

/* 3rd argument can be dropped completely */
substr(
    $path, strlen($pathPrefix)
);
mb_substr(
    $path, mb_strlen($pathPrefix)
);
mb_substr(
    $path, mb_strlen($pathPrefix), null, 'encoding'
);
