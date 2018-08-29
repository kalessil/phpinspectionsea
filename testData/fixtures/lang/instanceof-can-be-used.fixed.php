<?php

final class FinalClass               {}

class ParentClass                    {}
class ChildClass extends ParentClass {}

function cases_holder($object) {
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
