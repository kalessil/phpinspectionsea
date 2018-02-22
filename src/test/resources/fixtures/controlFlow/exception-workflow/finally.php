<?php

function cases_holder() {
    try {
    } finally {
        throw new <error descr="Exceptions management inside finally has a variety of side-effects in certain PHP versions.">\RuntimeException</error>();

        <error descr="Exceptions management inside finally has a variety of side-effects in certain PHP versions.">try</error> {
        } catch (\Exception $inner) {}
    }
}