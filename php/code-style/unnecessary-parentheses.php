<?php

    require_once ('whatever.php'); // <- reported

    (new stdClass())->with('data');                       // not reported
    (clone $stdClass)->with('data');                      // not reported
    /** @noinspection UsingInclusionReturnValueInspection */
    $mixedIncludeReturn = (include __DIR__ . '/foo.php'); // not reported

    $x = ($x) + 1; // -> reported
