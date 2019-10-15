<?php

    preg_match('/|.+|/U', '');
    preg_match('/|.*|/U', '');
    preg_match('/|.?|/U', '');

    preg_match(<weak_warning descr="[EA] 'U' modifier is ambiguous here ('*', '+' or '?' are missing in the given pattern).">'/[.*?]/U'</weak_warning>, '');
    preg_match(<weak_warning descr="[EA] 'U' modifier is ambiguous here ('*', '+' or '?' are missing in the given pattern).">'/|\\+|/U'</weak_warning>, '');
    preg_match(<weak_warning descr="[EA] 'U' modifier is ambiguous here ('*', '+' or '?' are missing in the given pattern).">'/|\\*|/U'</weak_warning>, '');
    preg_match(<weak_warning descr="[EA] 'U' modifier is ambiguous here ('*', '+' or '?' are missing in the given pattern).">'/|\\?|/U'</weak_warning>, '');
