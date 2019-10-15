<?php

    echo <weak_warning descr="[EA] 'strpos(...)' should be used instead (the pattern does not contain alphabet characters).">stripos('container',  '|')</weak_warning>;
    echo <weak_warning descr="[EA] 'strrpos(...)' should be used instead (the pattern does not contain alphabet characters).">strripos('container', '|')</weak_warning>;
    echo <weak_warning descr="[EA] 'strstr(...)' should be used instead (the pattern does not contain alphabet characters).">stristr('container', '|')</weak_warning>;

    echo stripos('container',  'b');
    echo strripos('container', 'b');
    echo stristr('container',  'b');
    echo stripos('container',  'й');
    echo strripos('container', 'й');
    echo stristr('container',  'й');