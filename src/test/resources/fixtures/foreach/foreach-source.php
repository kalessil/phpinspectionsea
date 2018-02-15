<?php

    /* @var $mixed mixed */
    foreach (
        <weak_warning descr="Expressions' type contains 'mixed', please specify possible types instead (best practices).">$mixed</weak_warning> as $value
    ) {
        is_int($value);
    }

    /* @var $unknown */
    foreach (
        <weak_warning descr="Expressions' type was not recognized, please check type hints.">$unknown</weak_warning> as $value
    ) {
        is_int($value);
    }
    foreach (
        <weak_warning descr="Expressions' type was not recognized, please check type hints.">$unknown->x</weak_warning> as $value
    ) {
        is_int($value);
    }
    foreach (
        <weak_warning descr="Expressions' type was not recognized, please check type hints.">$unknown[0]</weak_warning> as $value
    ) {
        is_int($value);
    }

    /* @var $object object */
    foreach (
        <weak_warning descr="Expressions' type contains 'object', please specify possible types instead (best practices).">$object</weak_warning> as $value
    ) {
        is_int($value);
    }

    /* @var $int int */
    foreach (
        <error descr="Can not iterate 'int' (re-check type hints).">$int</error> as $value
    ) {
        is_int($value);
    }

    /* @var $stdClass stdClass */
    foreach (
         <warning descr="Iterates over '\stdClass' properties (probably should implement one of Iterator interfaces).">$stdClass</warning> as $value
    ) {
        is_int($value);
    }