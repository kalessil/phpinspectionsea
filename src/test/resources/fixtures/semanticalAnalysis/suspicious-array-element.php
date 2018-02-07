<?php

    $array = [<warning descr="There is chance that it should be 'variable' here.">$variable</warning> => $variable];

    $array = ['variable' => $variable];
    $array = [$variable];
    $array = ['variable'];