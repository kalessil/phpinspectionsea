<?php

final class FinalClass               {}

class ParentClass                    {}
class ChildClass extends ParentClass {}

/**
 * @param mixed $mixed
 * @param string $string
 */
function cases_holder($object, $mixed, $string) {
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

        $object instanceof \stdClass,
        $object instanceof \stdClass,

        /* false-positives: string or mixed */
        in_array('stdClass', class_parents($mixed)),
        in_array('stdClass', class_parents($string)),
    ];
}
