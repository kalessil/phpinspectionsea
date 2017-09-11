<?php

    /** @param string[] $stringsArray */
    function arrayTypes (array $array, iterable $iterable, $stringsArray) {
        return
            is_array($array) ||
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
            $parameter = $this->returnSelf();
            $parameter = $this->returnStatic();
            $parameter = <warning descr="New value type (null) is not in annotated types.">null</warning>;
        }
    }