<?php

    (new stdClass())->with('data');                       // not reported
    $mixedIncludeReturn = (include __DIR__ . '/foo.php'); // not reported

    $x = ($x) + 1; // -> reported
