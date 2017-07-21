<?php

    /* @var object      $object */
    /* @var string|null $mixed */

    /* @var string  $string */
    /* @var array   $array */
    /* @var boolean $boolean */
    /* @var float   $float */
    /* @var integer $integer */
    return [
        (object) $object,
        (string) $mixed,

        <weak_warning descr="This type casting is not necessary, as the argument is of needed type.">(string) $string</weak_warning>,
        <weak_warning descr="This type casting is not necessary, as the argument is of needed type.">(array) $array</weak_warning>,
        <weak_warning descr="This type casting is not necessary, as the argument is of needed type.">(boolean) $boolean</weak_warning>,
        <weak_warning descr="This type casting is not necessary, as the argument is of needed type.">(float) $float</weak_warning>,
        <weak_warning descr="This type casting is not necessary, as the argument is of needed type.">(integer) $integer</weak_warning>,

        <weak_warning descr="This type casting is not necessary, as the argument is of needed type.">(integer) ($integer + 1)</weak_warning>
    ];

    /** @var string $string */
    function withWeakParameter($string) {
        return (string) $string;
    }
    function withStrictParameter(string $string) {
        return <weak_warning descr="This type casting is not necessary, as the argument is of needed type.">(string) $string</weak_warning>;
    }

    class ClassWithSomeMethods {
        /** @return string */
        function withWeakReturn() {
            return '';
        }
        function withStrictReturn(): string {
            return '';
        }
    }
    $instance = new ClassWithSomeMethods();
    return [
        (string) $instance->withWeakReturn(),
        <weak_warning descr="This type casting is not necessary, as the argument is of needed type.">(string) $instance->withStrictReturn()</weak_warning>
    ];