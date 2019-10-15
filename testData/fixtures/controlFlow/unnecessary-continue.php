<?php

function cases_holder() {
    foreach ([] as $value) {
        <warning descr="[EA] It's not really makes sense placing continue here as loop will continue from here anyway.">continue;</warning>
    }

    for (;;) {
        <warning descr="[EA] It's not really makes sense placing continue here as loop will continue from here anyway.">continue;</warning>
    }

    do {
        <warning descr="[EA] It's not really makes sense placing continue here as loop will continue from here anyway.">continue;</warning>
    } while (true);

    while (true) {
        <warning descr="[EA] It's not really makes sense placing continue here as loop will continue from here anyway.">continue;</warning>
    }

    foreach ([] as $value) {
        if ($value === 0) {
            <warning descr="[EA] It's not really makes sense placing continue here as loop will continue from here anyway.">continue;</warning>
        } elseif ($value === -1) {
            if ($value === -1) {
                <warning descr="[EA] It's not really makes sense placing continue here as loop will continue from here anyway.">continue;</warning>
            } else {
                <warning descr="[EA] It's not really makes sense placing continue here as loop will continue from here anyway.">continue;</warning>
            }
        } else {
            <warning descr="[EA] It's not really makes sense placing continue here as loop will continue from here anyway.">continue;</warning>
        }
    }

    foreach ([] as $value) {
        try {
            <warning descr="[EA] It's not really makes sense placing continue here as loop will continue from here anyway.">continue;</warning>
        } catch (Exception $exception) {
            <warning descr="[EA] It's not really makes sense placing continue here as loop will continue from here anyway.">continue;</warning>
        } finally {
            <warning descr="[EA] It's not really makes sense placing continue here as loop will continue from here anyway.">continue;</warning>
        }
    }

    /* false-positives: switch-case context */
    foreach ([] as $value) {
        switch ($value) {
            default:
                continue;
        }
    }

    /* false-positives: continue with arguments */
    foreach ([] as $value) {
        foreach ([] as $value) {
            continue 2;
        }
    }
}
