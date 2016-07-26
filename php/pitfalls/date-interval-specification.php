<?php

    /* reported */
    $x = new DateInterval('28D');
    $x = new DateInterval('PT28D');
    $x = new DateInterval('P28D1Y');
    $x = new DateInterval('PT00:00:01');


    /* not reported */
    $x = new DateInterval("P28D{$inline}");
    $x = new DateTime('28D');
    $x = new DateInterval('P28D');
    $x = new DateInterval('P0000-00-00T00:00:01');
