<?php

function cases_holder($index)
{
    return [
        <warning descr="'array_column([], 'id')' would fit more here (it also faster).">array_map(function ($array) { return $array['id']; }, [])</warning>,
        <warning descr="'array_column([], $index)' would fit more here (it also faster).">array_map(function ($array) use ($index) { return $array[$index]; }, [])</warning>,

        array_map(function ($array) use ($index) { return $array[trim($index)]; }, []),
    ];
}
