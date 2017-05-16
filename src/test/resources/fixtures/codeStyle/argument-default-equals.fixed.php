<?php

function method($a = 0, $b = 0) {
}

// Positives.
method();
method(1);
method(0, 1);

// Warnings.
method();
method(1);
method();
