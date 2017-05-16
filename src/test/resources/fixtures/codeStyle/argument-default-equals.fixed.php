<?php

function test($a = 0, $b = 0) {
}

// Positives.
test();
test(1);
test(0, 1);

// Warnings.
test();
test(1);
test();
