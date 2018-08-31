<?php

    echo <warning descr="'$base ** $exp' can be used instead">pow($base, $exp)</warning>;
    echo <warning descr="'(1 + $base) ** $exp' can be used instead">pow(1 + $base, $exp)</warning>;
    echo <warning descr="'$base ** (1 + $exp)' can be used instead">pow($base, 1 + $exp)</warning>;
    echo <warning descr="'(1 + $base) ** (1 + $exp)' can be used instead">pow(1 + $base, 1 + $exp)</warning>;
    echo 1 + <warning descr="'($base ** $exp)' can be used instead">pow($base, $exp)</warning>;

    echo <warning descr="'($base ?? 1) ** ($exp ?? 1)' can be used instead">pow($base ?? 1, $exp ?? 1)</warning>;
    echo <warning descr="'($base ?: 1) ** ($exp ?: 1)' can be used instead">pow($base ?: 1, $exp ?: 1)</warning>;