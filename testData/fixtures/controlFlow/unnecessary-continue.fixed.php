<?php

function cases_holder() {
    foreach ([] as $value) {
    }

    for (;;) {
    }

    do {
    } while (true);

    while (true) {
    }

    foreach ([] as $value) {
        if ($value === 0) {
        } elseif ($value === -1) {
            if ($value === -1) {
            } else {
            }
        } else {
        }
    }

    foreach ([] as $value) {
        try {
        } catch (Exception $exception) {
        } finally {
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
