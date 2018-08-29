<?php

final class FinalClass               {}

class ParentClass                    {}
class ChildClass extends ParentClass {}

function cases_holder($object) {
    return [
        <warning descr="'$object instanceof \stdClass' can be used instead.">get_class($object) == 'stdClass'</warning>,
        <warning descr="'! $object instanceof \stdClass' can be used instead.">get_class($object) != 'stdClass'</warning>,
        <warning descr="'$object instanceof \stdClass' can be used instead.">get_class($object) === 'stdClass'</warning>,
        <warning descr="'! $object instanceof \stdClass' can be used instead.">get_class($object) !== 'stdClass'</warning>,

        <warning descr="'$object instanceof \FinalClass' can be used instead.">get_class($object) == 'FinalClass'</warning>,
        <warning descr="'$object instanceof \ChildClass' can be used instead.">get_class($object) == 'ChildClass'</warning>,

        /* false-positives: has child classes */
        get_class($object) == 'ParentClass',

        /* false-positives: incomplete classes */
        get_class($object) == '__PHP_Incomplete_Class',
    ];
}
