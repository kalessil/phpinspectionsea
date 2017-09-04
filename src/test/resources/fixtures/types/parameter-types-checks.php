<?php

    /** @param string[] $stringsArray */
    function arrayTypes (array $array, iterable $iterable, $stringsArray) {
        return
            is_array($array) ||
            is_array($iterable) ||
            is_array($stringsArray) ||

            <weak_warning descr="Makes no sense, because this type is not defined in annotations.">is_int($array)</weak_warning> ||
            <weak_warning descr="Makes no sense, because this type is not defined in annotations.">is_int($iterable)</weak_warning> ||
            <weak_warning descr="Makes no sense, because this type is not defined in annotations.">is_int($stringsArray)</weak_warning> ||

            !<weak_warning descr="Makes no sense, because it's always true according to annotations.">is_int($array)</weak_warning> ||
            !<weak_warning descr="Makes no sense, because it's always true according to annotations.">is_int($iterable)</weak_warning> ||
            !<weak_warning descr="Makes no sense, because it's always true according to annotations.">is_int($stringsArray)</weak_warning>
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

        $second = <weak_warning descr="New value type (\Clazz) is not in annotated types.">$second ?? new Clazz()</weak_warning>;
        $second = <weak_warning descr="New value type (\Clazz) is not in annotated types.">$second ?: new Clazz()</weak_warning>;
        if (null === $second) {
            $second = <weak_warning descr="New value type (\Clazz) is not in annotated types.">new Clazz()</weak_warning>;
        }

        return [ $first, $second ];
    }