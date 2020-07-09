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

    <warning descr="[EA] Perhaps can be replaced with 'array_map(...)' call (reduces cognitive load).">foreach</warning> ([] as $index => $value) {
        $accumulator[$index] = trim($value);
    }

    <warning descr="[EA] Perhaps can be replaced with 'array_column(...)' call (reduces cognitive load).">foreach</warning> ([] as $index => $value) {
        $accumulator []= $value['...'];
    }
    <warning descr="[EA] Perhaps can be replaced with 'array_column(...)' call (reduces cognitive load).">foreach</warning> ([] as $index => $value) {
        $accumulator []= $value->property;
    }
    foreach ([] as $index => $value) {
        $accumulator[$index] = $value['...'];
    }

    <warning descr="[EA] Perhaps can be replaced with 'array_map(...)' call (reduces cognitive load).">foreach</warning> ([] as $index => $value) {
        $accumulator[$index] = (int) $value;
    }

    <warning descr="[EA] Perhaps can be replaced with 'array_filter(...)' call (reduces cognitive load).">foreach</warning> ($array as $index => $value) {
        if (empty($value)) { unset($array[$index]); }
    }
    <warning descr="[EA] Perhaps can be replaced with 'array_filter(...)' call (reduces cognitive load).">foreach</warning> ($array as $index => $value) {
        if (! $value) { unset($array[$index]); }
    }
    <warning descr="[EA] Perhaps can be replaced with 'array_filter(...)' call (reduces cognitive load).">foreach</warning> ($array as $index => $value) {
        if ($value == '') { unset($array[$index]); }
    }
    <warning descr="[EA] Perhaps can be replaced with 'array_filter(...)' call (reduces cognitive load).">foreach</warning> ($array as $index => $value) {
        if (empty($array[$index])) { unset($array[$index]); }
    }
    <warning descr="[EA] Perhaps can be replaced with 'array_filter(...)' call (reduces cognitive load).">foreach</warning> ($array as $index => $value) {
        if (! $array[$index]) { unset($array[$index]); }
    }
    <warning descr="[EA] Perhaps can be replaced with 'array_filter(...)' call (reduces cognitive load).">foreach</warning> ($array as $index => $value) {
        if ($array[$index] == '') { unset($array[$index]); }
    }

    <warning descr="[EA] Perhaps can be replaced with 'array_filter(...)' call (reduces cognitive load).">foreach</warning> ($array as $index => $value) {
        if (! empty($value)) { $storage[$index] = $array[$index]; }
    }
    <warning descr="[EA] Perhaps can be replaced with 'array_filter(...)' call (reduces cognitive load).">foreach</warning> ($array as $index => $value) {
        if ($value) { $storage[$index] = $array[$index]; }
    }
    <warning descr="[EA] Perhaps can be replaced with 'array_filter(...)' call (reduces cognitive load).">foreach</warning> ($array as $index => $value) {
        if ($value != '') { $storage[$index] = $array[$index]; }
    }
    <warning descr="[EA] Perhaps can be replaced with 'array_filter(...)' call (reduces cognitive load).">foreach</warning> ($array as $index => $value) {
        if (! empty($array[$index])) { $storage[$index] = $array[$index]; }
    }
    <warning descr="[EA] Perhaps can be replaced with 'array_filter(...)' call (reduces cognitive load).">foreach</warning> ($array as $index => $value) {
        if ($array[$index]) { $storage[$index] = $array[$index]; }
    }
    <warning descr="[EA] Perhaps can be replaced with 'array_filter(...)' call (reduces cognitive load).">foreach</warning> ($array as $index => $value) {
        if ($array[$index] != '') { $storage[$index] = $array[$index]; }
    }