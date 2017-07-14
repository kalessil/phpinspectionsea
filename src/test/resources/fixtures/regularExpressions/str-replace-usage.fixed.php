<?php

    str_replace("whatever", $replacement, $string);
    str_ireplace("whatever", $replacement, $string);

    /* false-positives */
    preg_replace('/whatever/',  $replacement, $string, 1);
    preg_replace('/^whatever/', $replacement, $string);
