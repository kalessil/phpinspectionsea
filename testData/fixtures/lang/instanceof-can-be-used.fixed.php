<?php

final class FinalClass               {}

class ParentClass                    {}
class ChildClass extends ParentClass {}

function get_class_cases_holder(\stdClass $object) {
    return [
        $object instanceof \stdClass,
        !$object instanceof \stdClass,
        $object instanceof \stdClass,
        !$object instanceof \stdClass,

        $object instanceof \FinalClass,
        $object instanceof \ChildClass,

        /* false-positives: has child classes */
        get_class($object) == 'ParentClass',

        /* false-positives: incomplete classes */
        get_class($object) == '__PHP_Incomplete_Class',
    ];
}

function get_parent_class_cases_holder(\stdClass $object, string $string) {
    return [
        $object instanceof \stdClass,
        !$object instanceof \stdClass,
        $object instanceof \stdClass,
        !$object instanceof \stdClass,

        $object instanceof \FinalClass,
        $object instanceof \ChildClass,
        $object instanceof \ParentClass,

        /* false-positives: string */
        get_parent_class($string) === 'stdClass',
        get_parent_class($string) == 'FinalClass',
    ];
}

function is_a_cases_holder(\stdClass $object, string $string) {
    return [
        $object instanceof \stdClass,
        $object instanceof \stdClass,

        $object instanceof \FinalClass,
        $object instanceof \ChildClass,
        $object instanceof \ParentClass,

        /* false-positives: string */
        is_a($string, 'stdClass'),
        is_a($string, 'FinalClass'),
        is_a($object, 'stdClass', true),
        is_a($object, 'FinalClass', true),
    ];
}

function is_subclass_of_cases_holder(\stdClass $object, string $string) {
    return [
        $object instanceof \stdClass,
        $object instanceof \stdClass,

        $object instanceof \FinalClass,
        $object instanceof \ChildClass,
        $object instanceof \ParentClass,

        /* false-positives: string */
        is_subclass_of($string, 'stdClass'),
        is_subclass_of($string, 'FinalClass'),
        is_subclass_of($object, 'stdClass', true),
        is_subclass_of($object, 'FinalClass', true),
    ];
}

function class_implements_cases_holder(\stdClass $object, string $string) {
    return [
        $object instanceof \stdClass,

        $object instanceof \FinalClass,
        $object instanceof \ChildClass,
        $object instanceof \ParentClass,

        /* false-positives: string */
        in_array('stdClass', class_implements($string)),
        in_array('FinalClass', class_implements($string)),
    ];
}

function class_parents_cases_holder(\stdClass $object, string $string) {
    return [
        $object instanceof \stdClass,

        $object instanceof \FinalClass,
        $object instanceof \ChildClass,
        $object instanceof \ParentClass,

        /* false-positives: string */
        in_array('stdClass', class_parents($string)),
        in_array('FinalClass', class_parents($string)),
    ];
}