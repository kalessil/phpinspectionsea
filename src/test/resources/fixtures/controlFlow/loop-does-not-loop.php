<?php

    /* simple cases: different loops with different control structs */
    <warning descr="This loop does not loop.">foreach</warning> ([] as $x) {
        break;
    }
    <warning descr="This loop does not loop.">for</warning> (;;) {
        break;
    }
    <warning descr="This loop does not loop.">while</warning> ($x) {
        return;
    }
    <warning descr="This loop does not loop.">do</warning> {
        throw $exception;
    } while ($x);


    /* more complex case with nested continuations */
    <warning descr="This loop does not loop.">foreach</warning> ([] as $x) {
        foreach ([] as $y) {
            foreach ([] as $z) {
                continue 2;
            }
        }
        break;
    }