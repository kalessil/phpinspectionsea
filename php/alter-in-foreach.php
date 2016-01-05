<?php

    function () {
        $source = array();

        foreach ($source as & $id => $element) { // <- reported &
            $source[$id] = $element + 1; // <- reported $source[$id]
        }
        unset($element, $id); // <- reported $element

        foreach ($source as $i1 => &$level1) {
            foreach ($source as $i2 => $level2) {
                foreach ($source as $i3 => $level3) {
                    echo $level1.$level2.$level3;
                }
            }
        }
        unset($level1, $level2, $level3);

        foreach ($source as & $el) {
            ++$el;
        }
    }

