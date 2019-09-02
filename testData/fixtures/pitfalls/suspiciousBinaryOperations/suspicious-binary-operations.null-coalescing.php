<?php

function returns_string(): string { return ''; }
function returns_object(): ?object { return null; }

$y = [
    <error descr="The operation results to '(int)$x', the right operand can be omitted.">(int)$x</error> ?? '...',
    (<error descr="The operation results to '(string)$x', the right operand can be omitted.">(string)$x</error>) ?? '...',
    (<error descr="The operation results to '!$x', the right operand can be omitted.">!$x</error>) ?? '...',
    <error descr="Due to 'returns_object($x)' used as left operand, using '?:' instead of '??' would make more sense.">returns_object($x)</error> ?? '...',

    $x ?? <error descr="The operation results to '(int)$x', the right operand can be omitted.">(int)$x</error> ?? '...',
    $x ?? <error descr="Due to 'returns_object($x)' used as left operand, using '?:' instead of '??' would make more sense.">returns_object($x)</error> ?? '...',

    returns_string() ?? ''
];
