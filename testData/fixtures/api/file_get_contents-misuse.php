<?php

    <warning descr="[EA] 'copy($from, $to)' would consume less cpu and memory resources here.">file_put_contents($to, file_get_contents($from))</warning>;
    <warning descr="[EA] 'copy($from, $to)' would consume less cpu and memory resources here.">file_put_contents($to, @file_get_contents($from))</warning>;
    <warning descr="[EA] 'md5_file($from)' would consume less cpu and memory resources here.">md5(file_get_contents($from))</warning>;
    <warning descr="[EA] 'sha1_file($from)' would consume less cpu and memory resources here.">sha1(file_get_contents($from))</warning>;
    <warning descr="[EA] 'hash_file('...', $from)' would consume less cpu and memory resources here.">hash('...', file_get_contents($from))</warning>;
    <warning descr="[EA] 'hash_hmac_file('...', $from, 'key')' would consume less cpu and memory resources here.">hash_hmac('...', file_get_contents($from), 'key')</warning>;

    /* false-positives: php streams */
    file_put_contents($to, file_get_contents('php://...'));

    /* false-positives: extra flags */
    file_put_contents($to, file_get_contents($from), $flags);
    file_put_contents($to, file_get_contents($from, $useIncludePath));

    /* false-positives: methods */
    file_put_contents($to, $x->file_get_contents($from));
    $x->file_put_contents($to, file_get_contents($from));