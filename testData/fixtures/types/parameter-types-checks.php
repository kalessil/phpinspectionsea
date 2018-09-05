<?php

    /** @param string[] $stringsArray */
    function arrayTypes (array $array, iterable $iterable, $stringsArray) {
        return
            <warning descr="Makes no sense, because of parameter type declaration.">is_array($array)</warning> ||
            is_array($iterable) ||
            is_array($stringsArray) ||

            <warning descr="Makes no sense, because this type is not defined in annotations.">is_int($array)</warning> ||
            <warning descr="Makes no sense, because this type is not defined in annotations.">is_int($iterable)</warning> ||
            <warning descr="Makes no sense, because this type is not defined in annotations.">is_int($stringsArray)</warning> ||

            !<warning descr="Makes no sense, because it's always true according to annotations.">is_int($array)</warning> ||
            !<warning descr="Makes no sense, because it's always true according to annotations.">is_int($iterable)</warning> ||
            !<warning descr="Makes no sense, because it's always true according to annotations.">is_int($stringsArray)</warning>
        ;
    };

    /** @param callable $callable */
    function callableTypes ($callable) {
        return [
            is_callable($callable),
            is_string($callable),
            is_array($callable)
        ];
    }

    /**
     *  @param mixed $mixed
     *  @param object $object
     */
    function mixedTypes ($mixed, $object) {
        return [
            is_int($mixed),
            is_int($object)
        ];
    }

    /**
     *  @param float  $float
     *  @param int    $int
     *  @param number $number
     */
    function numericTypes ($float, $int, $number) {
        return [
            is_int($int),
            is_float($float),
            is_int($number), is_float($number)
        ];
    }

    /** @var string $string */
    function stringTypeMagic ($string) {
        $string .= null;
        $string = $string . null;
        return $string;
    }


    interface ParentInterface                        {}
    interface ChildInterface extends ParentInterface {}
    class Clazz implements ChildInterface            {}
    class ChildClass extends Clazz                   {}

    function inheritedTypes (ParentInterface $first = null, ChildClass $second = null) {
        $first = $first ?? new Clazz();
        $first = $first ?: new Clazz();
        if (null === $first) {
            $first = new Clazz();
        }

        $second = <warning descr="New value type (\Clazz) is not in annotated types.">$second ?? new Clazz()</warning>;
        $second = <warning descr="New value type (\Clazz) is not in annotated types.">$second ?: new Clazz()</warning>;
        if (null === $second) {
            $second = <warning descr="New value type (\Clazz) is not in annotated types.">new Clazz()</warning>;
        }

        return [ $first, $second ];
    }


    /* case: self/static types support */
    interface IndirectClassReference {
        /** @return self */
        public function returnSelf();
        /** @return static */
        public function returnStatic();
    }
    abstract class IndirectClassReferenceTest implements IndirectClassReference {
        public function method(IndirectClassReference $parameter) {
            $parameter = $parameter ?? $this->returnSelf();
            $parameter = $this->returnSelf();

            $parameter = $parameter ?? $this->returnStatic();
            $parameter = $this->returnStatic();

            $parameter = <warning descr="New value type (null) is not in annotated types.">null</warning>;
        }
    }

    /* false-positive: incomplete types influenced by null as default value */
    function incomplete_types($one = null, $two = []) {
        $one = '';
        $two = <warning descr="New value type (string) is not in annotated types.">''</warning>;
    }

    /* false-positive: core functions returning string|false, string|null */
    function core_api_functions_consistency(string $string) {
        $string = substr($string, -1);
        $string = preg_replace('', '', $string);
    }

    /* false-positive: nullable objects */
    function returns_nullable_object(): ?stdClass {}
    function assigning_nullable_objects(stdClass $object) {
        $object = returns_nullable_object();
    }

    /* false-positive: iterable */
    function iterable_support(iterable $iterable = null) {
        $iterable = $iterable ?? [];
    }

    /* false-positives: issues with proper binary expression types identification */
    function binary_expression_types(int $parameter) {
        $parameter = 1 * $parameter * 1;
        $parameter = <warning descr="New value type (float) is not in annotated types.">1 * 1.0 * $parameter * 1</warning>;
    }