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
        (int)(1 * 0.99 * 1)
    ];

    /** @var string $string */
    function withWeakParameter($string) {
        return (string) $string;
    }
    function withStrictParameter(string $string) {
        return $string;
    }

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
        $instance->withStrictReturn()
    ];

    /* false-positives: globals type resolving, null coalesce is problematic in general */
    function cases_holder_null_coalesce() {
        $optional = (int)($_GET['parameter'] ?? -1);
        $optional = (int)($whatever ?? 0);
    }