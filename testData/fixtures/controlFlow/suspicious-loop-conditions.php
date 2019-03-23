<?php

    /* case: anomalies in conditions, which conflicts with looping itself */
    if (!$wrong || $correct) {
        foreach ($correct as $value) {}
        <error descr="A parent condition '!$wrong' looks suspicious.">foreach</error> ($wrong as $value) {}
    }
    if (empty($wrong) || !empty($correct)) {
        foreach ($correct as $value) {}
        <error descr="A parent condition 'empty($wrong)' looks suspicious.">foreach</error> ($wrong as $value) {}
    }
    if ($wrong === [] || $correct !== []) {
        foreach ($correct as $value) {}
        <error descr="A parent condition '$wrong === []' looks suspicious.">foreach</error> ($wrong as $value) {}
    }
    if (!count($wrong) || count($correct)) {
        foreach ($correct as $value) {}
        <error descr="A parent condition '!count($wrong)' looks suspicious.">foreach</error> ($wrong as $value) {}
    }
    if (count($one) < 0 || count($two) <= 0 || count($three) == 0 || count($four) === 0 || count($correct) > 0) {
        foreach ($correct as $value) {}
        <error descr="A parent condition 'count($one) < 0' looks suspicious.">foreach</error> ($one as $value) {}
        <error descr="A parent condition 'count($two) <= 0' looks suspicious.">foreach</error> ($two as $value) {}
        <error descr="A parent condition 'count($three) == 0' looks suspicious.">foreach</error> ($three as $value) {}
        <error descr="A parent condition 'count($four) === 0' looks suspicious.">foreach</error> ($four as $value) {}
    }
    if (count($one) < 1 || count($two) <= 1 || count($three) == 1 || count($four) === 1 || count($correct) > 0) {
        foreach ($correct as $value) {}
        <error descr="A parent condition 'count($one) < 1' looks suspicious.">foreach</error> ($one as $value) {}
        <error descr="A parent condition 'count($two) <= 1' looks suspicious.">foreach</error> ($two as $value) {}
        <error descr="A parent condition 'count($three) == 1' looks suspicious.">foreach</error> ($three as $value) {}
        <error descr="A parent condition 'count($four) === 1' looks suspicious.">foreach</error> ($four as $value) {}
    }
    if (count($one) < 2 || count($correct) === 2) {
        foreach ($correct as $value) {}
        <error descr="A parent condition 'count($one) < 2' looks suspicious.">foreach</error> ($one as $value) {}
    } elseif (count($two) < 2) {
        foreach ($one as $value) {}
        <error descr="A parent condition 'count($two) < 2' looks suspicious.">foreach</error> ($two as $value) {}
    } else {
        foreach ($one as $value) {}
    }
    /* related false-positives */
    if (count($one) < 2) {
        $one = [];
        foreach ($one as $value) {}
    } elseif (count($two) < 2) {
        $one = [];
        foreach ($one as $value) {}
    } else {
        foreach ($one as $value) {}
    }