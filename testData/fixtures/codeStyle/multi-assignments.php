<?php

function cases_holder()
{
    return [
        <weak_warning descr="Using dedicated assignment would be more reliable (e.g '$... = $... + 10' can be mistyped as `$... = $... = 10`).">$a = $b = 0</weak_warning>,
    ];
}