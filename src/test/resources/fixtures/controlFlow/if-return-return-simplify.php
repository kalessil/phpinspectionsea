<?php

function suggestSimplifyingInverted($a)
{
    <weak_warning descr="An if-return construct can be replaced with 'return !($a > 0)'.">if</weak_warning> ($a > 0) {
        return false;
    }
    return true;
}

function suggestSimplifying($a)
{
    <weak_warning descr="An if-return construct can be replaced with 'return $a > 0'.">if</weak_warning> ($a > 0) {
        return true;
    }
    return false;
}

function noFalsePositivesCase($a)
{
    if ($a === 0) {
        return false;
    }
    /* comment: ensures that comments are not processed */
    <weak_warning descr="If and following return can be replaced with 'return !($a > 0)'.">if</weak_warning> ($a > 0) {
        return false;
    }
    return true;
}