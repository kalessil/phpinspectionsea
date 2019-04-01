<?php

use mktime;

function cases_holder() {
    time();
    mktime(0, 0, 0, 0, 0, 0, -1);
    time();
    gmmktime(0, 0, 0, 0, 0, 0, -1);

    /* false-positives */
    mktime(0);
    gmmktime(0);
}

