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
        (int)(1 * 0.99),
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