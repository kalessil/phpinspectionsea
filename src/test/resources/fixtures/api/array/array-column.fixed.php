<?php

function cases_holder($index)
{
    return [
        array_column([], 'id'),
        array_column([], $index),
    ];
}
