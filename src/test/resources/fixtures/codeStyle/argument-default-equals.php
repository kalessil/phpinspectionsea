<?php

function method($a = 0, $b = 0) {
}

// Positives.
method();
method(1);
method(0, 1);

// Warnings.
method(<weak_warning descr="This parameter could be dropped, because the value is the same from default value.">0</weak_warning>);
method(1, <weak_warning descr="This parameter could be dropped, because the value is the same from default value.">0</weak_warning>);
method(<weak_warning descr="This parameter could be dropped, because the value is the same from default value.">0, 0</weak_warning>);
