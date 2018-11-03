<?php

function cases_holder($source)
{
    <weak_warning descr="Second parameter should be provided to clarify keys collisions handling.">iterator_to_array($source)</weak_warning>;
    <weak_warning descr="Second parameter should be provided to clarify keys collisions handling.">iterator_to_array($source)</weak_warning>[0];

    /* false-positives: multiple cases */
    iterator_to_array($source, false);
    iterator_to_array($source, true);
}