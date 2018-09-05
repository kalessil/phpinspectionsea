<?php

    $x = <weak_warning descr="'array_values(array_unique([]))' would be more readable here (array_unique(...) was optimized in PHP 7.2-beta3+).">array_keys(array_count_values([]))</weak_warning>;
    $y = <weak_warning descr="'count(array_unique([]))' would be more readable here (array_unique(...) was optimized in PHP 7.2-beta3+).">count(array_count_values([]))</weak_warning>;