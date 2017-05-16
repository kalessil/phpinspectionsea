<?php

function test($a = 0, $b = 0) {
}

// Positives.
test();
test(1);
test(0, 1);

// Warnings.
test(<weak_warning descr="The argument can be safely dropped, as identical to the default value.">0</weak_warning>);
test(1, <weak_warning descr="The argument can be safely dropped, as identical to the default value.">0</weak_warning>);
test(<weak_warning descr="The argument can be safely dropped, as identical to the default value.">0, 0</weak_warning>);
