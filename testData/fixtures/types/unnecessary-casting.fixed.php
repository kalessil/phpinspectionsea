<?php

    /* @var object      $object */
    /* @var string|null $mixed */

    $string  = '...';
    $array   = [];
    $boolean = true;
    $float   = 0.0;
    $integer = 0;

    return [
        (object) $object,
        (string) $mixed,

        $string,
        $array,
        $boolean,
        $float,
        $integer,

        ($integer + 1),

        /* workaround for WI-37466 */
        (0.99 * 1),
        (1 * 0.99),
        (int)(1 * 0.99),
        (int)(0.99 * 1),
        (int)(1 * $unknown),
        (int)(1 * 0.99 * 1),

        .0,
        0.0,
        -.0,
        -0.0,
    ];

    /** @var string $string */
    function withWeakParameter($string) {
        return (string) $string;
    }
    function withStrictParameter(string $string) {
        return $string;
    }

    /** @return string */
    function with_weak_return()           { return ''; }
    function with_strict_return(): string { return ''; }

    class ClassWithSomeMethods {
        /** @var string */
        private $private;
        /** @var string */
        protected $protected;

        /** @return string */
        function withWeakReturn() {
            return (string) $this->protected;
        }
        function withStrictReturn(): string {
            return $this->private;
        }
    }

    $instance = new ClassWithSomeMethods();
    return [
        (string) $instance->withWeakReturn(),
        $instance->withStrictReturn(),
        (string) with_weak_return(),
        with_strict_return(),
    ];

    /* false-positives: globals type resolving, null coalesce is problematic in general */
    function cases_holder_null_coalesce() {
        $optional = (int)($_GET['parameter'] ?? -1);
        $optional = (int)($whatever ?? 0);
    }

    function cases_holder_concatenations($one, $two) {
        return [
            $one .= $one,
            $two . '...',
        ];
    }

    function cases_holder_assignments() {
        return [
            (int) ($one = (0.99 * 1)),
            ($two = (0.99 * 1)),
        ];
    }

    function cases_argument_specific_return() {
        return [
            (int) (microtime(true) * 1000),
            (microtime() * 1),
            (microtime(false) * 1),
        ];
    }

    /* false-positives: untyped properties in ternaries/elvis operators */
    class UntypedPropertyConsumer extends UntypedPropertyHolder {
        public function method() {
            return (int)($this->property['index'] ?? 0);
        }
    }
    class UntypedPropertyHolder {
        protected $property = [];
    }

    /* false-positives: operations with strings */
    function cases_holder_string_as_operation_argument(string $string) {
        return [
            (int) ($string * 100),
            (int) ($string / 100),
        ];
    }

    /* false-positives: core APIs */
    function cases_holder_core_apis() {
        return (bool) current([]);
    }