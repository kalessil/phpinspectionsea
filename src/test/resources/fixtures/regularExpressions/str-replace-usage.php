<?php

    <warning descr="'str_replace(\"whatever\", $replacement, $string)' can be used instead.">preg_replace('/whatever/',  $replacement, $string)</warning>;
    <warning descr="'str_ireplace(\"whatever\", $replacement, $string)' can be used instead.">preg_replace('/whatever/i', $replacement, $string)</warning>;

    /* false-positives */
    preg_replace('/whatever/',  $replacement, $string, 1);
    preg_replace('/^whatever/', $replacement, $string);
