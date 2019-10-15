<?php

    echo <warning descr="[EA] '$base ** $exp' can be used instead">pow($base, $exp)</warning>;
    echo <warning descr="[EA] '(1 + $base) ** $exp' can be used instead">pow(1 + $base, $exp)</warning>;
    echo <warning descr="[EA] '$base ** (1 + $exp)' can be used instead">pow($base, 1 + $exp)</warning>;
    echo <warning descr="[EA] '(1 + $base) ** (1 + $exp)' can be used instead">pow(1 + $base, 1 + $exp)</warning>;
    echo 1 + <warning descr="[EA] '($base ** $exp)' can be used instead">pow($base, $exp)</warning>;

    echo <warning descr="[EA] '($base ?? 1) ** ($exp ?? 1)' can be used instead">pow($base ?? 1, $exp ?? 1)</warning>;
    echo <warning descr="[EA] '($base ?: 1) ** ($exp ?: 1)' can be used instead">pow($base ?: 1, $exp ?: 1)</warning>;