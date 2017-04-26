<?php

    <weak_warning descr="Confusing statement: consider re-factoring.">return $x = function() {};</weak_warning>

    function multipleReturnsFunction($x) {
        if (0 === $x) {
            return;
        }

        if ($x > 0) {
            return;
        }

        <weak_warning descr="Senseless statement: return null implicitly or safely remove it.">return;</weak_warning>
    }