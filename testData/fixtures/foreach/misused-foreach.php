<?php

    <warning descr="[EA] Perhaps can be replaced with 'implode(...)' call (reduces cognitive load).">foreach</warning> ([] as $index => $value) {
        $accumulator .= $value;
    }

    <warning descr="[EA] Perhaps can be replaced with 'implode(...)' call (reduces cognitive load).">foreach</warning> ([] as $index => $value) {
        $accumulator .= '...' . $value;
    }

    <warning descr="[EA] Perhaps can be replaced with 'array_flip(...)' call (reduces cognitive load).">foreach</warning> ([] as $index => $value) {
        $array[$value] = $index;
    }

    <warning descr="[EA] Perhaps can be replaced with 'array_sum(...)' call (reduces cognitive load).">foreach</warning> ([] as $value) {
        $accumulator += $value;
    }

    <warning descr="[EA] Perhaps can be replaced with 'array_product(...)' call (reduces cognitive load).">foreach</warning> ([] as $value) {
        $accumulator *= $value;
    }