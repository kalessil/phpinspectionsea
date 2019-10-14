<?php

    <warning descr="[EA] 'time()' should be used instead (2x faster).">strtotime('now')</warning>;
    <warning descr="[EA] 'time()' should be used instead (2x faster).">strtotime('NOW')</warning>;

    strtotime('+1 day');
    <warning descr="[EA] 'time()' is default valued already, it can safely be removed.">strtotime('+1 day', time())</warning>;