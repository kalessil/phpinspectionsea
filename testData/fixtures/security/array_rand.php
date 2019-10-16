<?php

function cases_holder() {
    $value = $values[<error descr="[EA] Insufficient entropy, random_int/random_bytes based solution would be more secure.">array_rand</error>($values)];

    $values = $values[array_rand($values, 5)];
    $key    = array_rand($values);
}