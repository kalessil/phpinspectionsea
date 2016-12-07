<?php

    <warning descr="You shall use time() function instead (2x faster)">strtotime('now')</warning>;
    <warning descr="You shall use time() function instead (2x faster)">strtotime('NOW')</warning>;

    strtotime('+1 day');
    strtotime('+1 day', <warning descr="'time()' is a default valued already, safely drop it">time()</warning>);