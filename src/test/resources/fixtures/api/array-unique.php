<?php

    $x = <weak_warning descr="'array_keys(array_count_values([]))' would be more efficient (make sure to leave a comment to explain the intent).">array_values(array_unique([]))</weak_warning>;
    $y = <weak_warning descr="'count(array_count_values([]))' would be more efficient (make sure to leave a comment to explain the intent).">count(array_unique([]))</weak_warning>;