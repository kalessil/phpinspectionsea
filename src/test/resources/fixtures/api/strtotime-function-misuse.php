<?php

    <warning descr="'time()' can be used instead (2x faster)">strtotime('now')</warning>;
    <warning descr="'time()' can be used instead (2x faster)">strtotime('NOW')</warning>;

    strtotime('+1 day');
    strtotime('+1 day', <warning descr="'time()' is a default valued already, safely drop it">time()</warning>);