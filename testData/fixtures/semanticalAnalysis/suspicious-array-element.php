<?php

    $array = [<warning descr="[EA] There is chance that it should be 'variable' here.">$variable</warning> => $variable];

    $array = ['variable' => $variable];
    $array = [$variable];
    $array = ['variable'];