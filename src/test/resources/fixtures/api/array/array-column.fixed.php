<?php

function cases_holder($index)
{
    return [
        array_column([], 'id'),
        array_column([], $index),

        array_map(function ($array) use ($index) { return $array[trim($index)]; }, []),
    ];
}
