<?php

    function () {
        $source = array();

        foreach ($source as & $id => $element) { // <- reported &
            $source[$id] = $element + 1; // <- reported $source[$id]
        }
        unset($element, $id); // <- reported $element

        foreach ($source as & $el) {
            ++$el;
        }
    }

