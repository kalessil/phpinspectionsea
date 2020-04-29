<?php

    $array = [
        'variable' => $variable,
        'indexWithLeadingSpace' => '...',
        'indexWithTrailingSpace' => '...',
        $source['indexWithLeadingSpace'],
        $source['indexWithTrailingSpace'],
    ];

    $array = [' ' => '...'];
    $array = [' and ' => '...'];
    $array = ['variable' => $variable];
    $array = [$variable];
    $array = ['variable'];