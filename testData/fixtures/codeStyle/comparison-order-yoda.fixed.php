<?php

/* operators check */
$x = '' == $y;
$x = '' != $y;
$x = '' === $y;
$x = '' !== $y;

/* expressions type */
$x = '' == $y;
$x = __DIR__ == $y;
$x = 0 == $y;

/* false positives */
$x = '' === $y;
$x = __DIR__ ==  '';
$x = 0 == '';