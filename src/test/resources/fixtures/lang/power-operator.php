<?php

    echo <weak_warning descr="'$base ** $exp' can be used instead">pow($base, $exp)</weak_warning>;
    echo 1 + <weak_warning descr="'($base ** $exp)' can be used instead">pow($base, $exp)</weak_warning>;