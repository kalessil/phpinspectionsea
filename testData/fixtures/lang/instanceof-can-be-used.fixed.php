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
