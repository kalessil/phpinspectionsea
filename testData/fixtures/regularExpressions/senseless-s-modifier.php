<?php

    preg_match('/[a-z]./s', '');
    preg_match('/|.+|/s', '');
    preg_match('/|.*|/s', '');
    preg_match('/\[ea:...\]/s', '');

    preg_match(<weak_warning descr="[EA] 's' modifier is ambiguous here ('.' is missing in the given pattern).">'/(prefix)[a-z](suffix)/s'</weak_warning>, '');
    preg_match(<weak_warning descr="[EA] 's' modifier is ambiguous here ('.' is missing in the given pattern).">'/(prefix)[a-z.](suffix)/s'</weak_warning>, '');
    preg_match(<weak_warning descr="[EA] 's' modifier is ambiguous here ('.' is missing in the given pattern).">'/(prefix)[a-z]\.(suffix)/s'</weak_warning>, '');