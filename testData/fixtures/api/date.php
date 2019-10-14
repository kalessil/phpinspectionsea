<?php

    date('...', <weak_warning descr="[EA] 'time()' is default valued already, it can safely be removed.">time()</weak_warning>);

    date('...', time('...'));
    date('...', $object->time());