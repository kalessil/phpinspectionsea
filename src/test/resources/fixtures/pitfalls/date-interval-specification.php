<?php

    /* reported */
    $x = new DateInterval(<error descr="Date interval specification seems to be invalid.">'28D'</error>);
    $x = new DateInterval(<error descr="Date interval specification seems to be invalid.">'PT28D'</error>);
    $x = new DateInterval(<error descr="Date interval specification seems to be invalid.">'P28D1Y'</error>);
    $x = new DateInterval(<error descr="Date interval specification seems to be invalid.">'PT00:00:01'</error>);
    $x = new DateInterval(<error descr="Date interval specification seems to be invalid.">'PT'</error>);

    /* not reported */
    $x = new DateInterval("P28D{$inline}");
    $x = new DateTime('28D');
    $x = new DateInterval('P28D');
    $x = new DateInterval('P0000-00-00T00:00:01');
