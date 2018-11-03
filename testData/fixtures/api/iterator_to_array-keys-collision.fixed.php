<?php

function cases_holder($source)
{
    iterator_to_array($source, false);
    iterator_to_array($source, false)[0];

    /* false-positives: multiple cases */
    iterator_to_array($source, false);
    iterator_to_array($source, true);
}