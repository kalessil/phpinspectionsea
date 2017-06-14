<?php

    /** @param string[] $stringsArray */
    function arrayTypes (array $array, iterable $iterable, $stringsArray) {
        return
            is_array($array) ||
            is_array($iterable) ||
            is_array($stringsArray) ||

            <weak_warning descr="Makes no sense, because this type is not defined in annotations.">is_int</weak_warning>($array) ||
            <weak_warning descr="Makes no sense, because this type is not defined in annotations.">is_int</weak_warning>($iterable) ||
            <weak_warning descr="Makes no sense, because this type is not defined in annotations.">is_int</weak_warning>($stringsArray) ||

            !<weak_warning descr="Makes no sense, because it's always true according to annotations.">is_int</weak_warning>($array) ||
            !<weak_warning descr="Makes no sense, because it's always true according to annotations.">is_int</weak_warning>($iterable) ||
            !<weak_warning descr="Makes no sense, because it's always true according to annotations.">is_int</weak_warning>($stringsArray)
        ;
    };

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