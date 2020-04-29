<?php

    $array = [
        <warning descr="[EA] There is chance that it should be 'variable' here.">$variable</warning> => $variable,
        <warning descr="[EA] There is chance that it should be 'indexWithLeadingSpace' here.">' indexWithLeadingSpace'</warning> => '...',
        <warning descr="[EA] There is chance that it should be 'indexWithTrailingSpace' here.">'indexWithTrailingSpace '</warning> => '...',
        $source[<warning descr="[EA] There is chance that it should be 'indexWithLeadingSpace' here.">' indexWithLeadingSpace'</warning>],
        $source[<warning descr="[EA] There is chance that it should be 'indexWithTrailingSpace' here.">'indexWithTrailingSpace '</warning>],
    ];

    $array = ['variable' => $variable];
    $array = [$variable];
    $array = ['variable'];