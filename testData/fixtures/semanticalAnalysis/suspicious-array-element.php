<?php

    $array = [
        <warning descr="[EA] There is chance that it should be 'variable' here.">$variable</warning> => $variable,
        <warning descr="[EA] There is chance that it should be 'indexWithLeadingSpace' here (without leading/trailing space).">' indexWithLeadingSpace'</warning> => '...',
        <warning descr="[EA] There is chance that it should be 'indexWithTrailingSpace' here (without leading/trailing space).">'indexWithTrailingSpace '</warning> => '...',
        $source[<warning descr="[EA] There is chance that it should be 'indexWithLeadingSpace' here (without leading/trailing space).">' indexWithLeadingSpace'</warning>],
        $source[<warning descr="[EA] There is chance that it should be 'indexWithTrailingSpace' here (without leading/trailing space).">'indexWithTrailingSpace '</warning>],
    ];

    $array = ['variable' => $variable];
    $array = [$variable];
    $array = ['variable'];