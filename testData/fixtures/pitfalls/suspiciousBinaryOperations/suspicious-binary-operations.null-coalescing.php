<?php

function returns_string(): string { return ''; }
function returns_object(): ?object { return null; }

$y = [
    <error descr="[EA] The operation results to '(int)$x', the right operand can be omitted.">(int)$x</error> ?? '...',
    (<error descr="[EA] The operation results to '(string)$x', the right operand can be omitted.">(string)$x</error>) ?? '...',
    (<error descr="[EA] The operation results to '!$x', the right operand can be omitted.">!$x</error>) ?? '...',

    returns_object($x) ?? '...',

    $x ?? <error descr="[EA] The operation results to '(int)$x', the right operand can be omitted.">(int)$x</error> ?? '...',

    $x ?? returns_object($x) ?? '...',
    returns_string() ?? '',

    <error descr="[EA] The operation results to '$defined . $undefined', the right operand can be omitted.">$defined . $undefined</error> ?? 'alternative',
    $defined ?? <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">'alternative' . '...'</error>,
    $undefined ?? $undefined ?? 'alternative',
];
