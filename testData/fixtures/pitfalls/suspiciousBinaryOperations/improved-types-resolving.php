<?php

function test_cases() {
    $strings = [ '...' ];
    return [
         current($strings) === '...',
         current($strings) === false,
         <error descr="[EA] 'current($strings) === []' seems to be always false.">current($strings) === []</error>,

         array_pop($strings) === '...',
         array_pop($strings) === null,
         <error descr="[EA] 'array_pop($strings) === []' seems to be always false.">array_pop($strings) === []</error>,

         array_unique($strings) === [],
         <error descr="[EA] 'array_unique($strings) === false' seems to be always false.">array_unique($strings) === false</error>,
    ];
}