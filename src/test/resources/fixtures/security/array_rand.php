<?php

    $value = $values[<error descr="Insufficient entropy, mt_rand/random_int based solution would be more secure.">array_rand($values)</error>];

    $values = $values[array_rand($values, 5)];
    $key    = array_rand($values);