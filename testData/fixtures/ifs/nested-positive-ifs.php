<?php

function cases_holder_if_merge() {
    if ($a) {
        if ($b) {}
    } else {
        /* alternative branch case */
    }

    if ($a) {
        if ($b) {}
        else    {}
    }

    if ($a) {
       <weak_warning descr="[EA] If construct can be merged with parent one.">if</weak_warning> ($b) {
       }
    }

    if ($a && $b) {
       <weak_warning descr="[EA] If construct can be merged with parent one.">if</weak_warning> ($c) {
       }
    }

    if ($a) {
       <weak_warning descr="[EA] If construct can be merged with parent one.">if</weak_warning> ($b && $c) {
       }
    }

    if ($a || $b) {
        if ($c || $d) {
        }
    }

    if ($a || $b) {
        if ($c) {
        }
    }

    if ($a) {
        if ($b || $c) {
        }
    }
}

function cases_holder_else_merge() {
    if ($a) {}
    else {
        <weak_warning descr="[EA] If construct can be merged with parent one.">if</weak_warning> ($b) {}
    }

    if ($a) {}
    else {
        <weak_warning descr="[EA] If construct can be merged with parent one.">if</weak_warning> ($b) {}
        else {}
    }
}

function cases_holder_same_else_merge() {
    if ($a) {
        <weak_warning descr="[EA] If construct can be merged with parent one.">if</weak_warning> ($b) {}
        else {}
    } else {}

    if ($a) {
        if ($b) {}
        else {}
    } else { ; }
}

function cases_holder_operations_priority() {
    if ($a = 0) {
        <weak_warning descr="[EA] If construct can be merged with parent one.">if</weak_warning> ($b) {}
    }

    if ($a ?: 0) {
        <weak_warning descr="[EA] If construct can be merged with parent one.">if</weak_warning> ($b) {}
    }

    if ($a ?? 0) {
        <weak_warning descr="[EA] If construct can be merged with parent one.">if</weak_warning> ($b) {}
    }
}

function preserve_comments_in_fix() {
    if ($a) {
        // comment
        <weak_warning descr="[EA] If construct can be merged with parent one.">if</weak_warning> ($b) {}
    }
}