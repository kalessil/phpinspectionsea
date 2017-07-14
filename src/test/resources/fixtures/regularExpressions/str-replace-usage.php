<?php

    <warning descr="'str_replace(\"whatever\", ...)' can be used instead.">preg_replace</warning>('/whatever/',  $replacement, $string);
    <warning descr="'str_ireplace(\"whatever\", ...)' can be used instead.">preg_replace</warning>('/whatever/i', $replacement, $string);

    /* false-positives */
    preg_replace('/whatever/',  $replacement, $string, 1);
    preg_replace('/^whatever/', $replacement, $string);
