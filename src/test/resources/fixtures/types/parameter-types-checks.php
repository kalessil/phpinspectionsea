<?php

    /** @param string[] $stringsArray */
    function arrayTypes (array $array, iterable $iterable, $stringsArray) {
        return
            is_array($array) ||
            is_array($iterable) ||
            is_array($stringsArray) ||
            <weak_warning descr="Makes no sense, because this type is not defined in annotations.">is_int</weak_warning>($array) ||
            <weak_warning descr="Makes no sense, because this type is not defined in annotations.">is_int</weak_warning>($iterable) ||
            <weak_warning descr="Makes no sense, because this type is not defined in annotations.">is_int</weak_warning>($stringsArray)
        ;
    };