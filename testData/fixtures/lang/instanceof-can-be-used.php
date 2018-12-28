<?php

final class FinalClass               {}

class ParentClass                    {}
class ChildClass extends ParentClass {}

function get_class_cases_holder(\stdClass $object) {
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

function get_parent_class_cases_holder(\stdClass $object, string $string) {
    return [
        <warning descr="'$object instanceof \stdClass' can be used instead.">get_parent_class($object) == 'stdClass'</warning>,
        <warning descr="'! $object instanceof \stdClass' can be used instead.">get_parent_class($object) != 'stdClass'</warning>,
        <warning descr="'$object instanceof \stdClass' can be used instead.">get_parent_class($object) === 'stdClass'</warning>,
        <warning descr="'! $object instanceof \stdClass' can be used instead.">get_parent_class($object) !== 'stdClass'</warning>,

        <warning descr="'$object instanceof \FinalClass' can be used instead.">get_parent_class($object) == 'FinalClass'</warning>,
        <warning descr="'$object instanceof \ChildClass' can be used instead.">get_parent_class($object) == 'ChildClass'</warning>,
        <warning descr="'$object instanceof \ParentClass' can be used instead.">get_parent_class($object) == 'ParentClass'</warning>,

        /* false-positives: string */
        get_parent_class($string) === 'stdClass',
        get_parent_class($string) == 'FinalClass',
    ];
}
