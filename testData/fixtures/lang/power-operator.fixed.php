<?php

    echo $base ** $exp;
    echo (1 + $base) ** $exp;
    echo $base ** (1 + $exp);
    echo (1 + $base) ** (1 + $exp);
    echo 1 + ($base ** $exp);

    echo ($base ?? 1) ** ($exp ?? 1);
    echo ($base ?: 1) ** ($exp ?: 1);