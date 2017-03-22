<?php

    echo <weak_warning descr="'$base ** $exp' can be used instead">pow($base, $exp)</weak_warning>;
    echo <weak_warning descr="'(1 + $base) ** $exp' can be used instead">pow(1 + $base, $exp)</weak_warning>;
    echo <weak_warning descr="'$base ** (1 + $exp)' can be used instead">pow($base, 1 + $exp)</weak_warning>;
    echo <weak_warning descr="'(1 + $base) ** (1 + $exp)' can be used instead">pow(1 + $base, 1 + $exp)</weak_warning>;
    echo 1 + <weak_warning descr="'($base ** $exp)' can be used instead">pow($base, $exp)</weak_warning>;