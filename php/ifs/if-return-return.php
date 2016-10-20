<?php

function IRRReported()
{
    $a = 0;

    if ($a > 0) {     // <- reported
        return false;
    }

    return true;
}

function IRRNotReported()
{
    $a = 0;

    if ($a === 0) {
        return false;
    }

    if ($a > 0) {      // <- NOT reported
        return false;
    }

    return true;
}