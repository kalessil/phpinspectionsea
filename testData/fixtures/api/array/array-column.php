<?php

function cases_holder($index)
{
    return [
        <warning descr="'array_column([], 'id')' would fit more here (it also faster, but loses original keys).">array_map(function ($array) { return $array['id']; }, [])</warning>,
        <warning descr="'array_column([], $index)' would fit more here (it also faster, but loses original keys).">array_map(function ($array) use ($index) { return $array[$index]; }, [])</warning>,

        <warning descr="'array_column([], 'property')' would fit more here (it also faster, but loses original keys).">array_map(function ($object) { return $object->property; }, [])</warning>,
        <warning descr="'array_column([], $property)' would fit more here (it also faster, but loses original keys).">array_map(function ($object) use ($index) { return $object->$property; }, [])</warning>,
        <warning descr="'array_column([], $property)' would fit more here (it also faster, but loses original keys).">array_map(function ($object) use ($index) { return $object->{$property}; }, [])</warning>,
    ];
}
