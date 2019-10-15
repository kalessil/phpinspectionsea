<?php

final class FinalClass               {}

class ParentClass                    {}
class ChildClass extends ParentClass {}

function get_class_cases_holder(\stdClass $object) {
    return [
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">get_class($object) == 'stdClass'</warning>,
        <warning descr="[EA] '! $object instanceof \stdClass' can be used instead.">get_class($object) != 'stdClass'</warning>,
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">get_class($object) === 'stdClass'</warning>,
        <warning descr="[EA] '! $object instanceof \stdClass' can be used instead.">get_class($object) !== 'stdClass'</warning>,

        <warning descr="[EA] '$object instanceof \FinalClass' can be used instead.">get_class($object) == 'FinalClass'</warning>,
        <warning descr="[EA] '$object instanceof \ChildClass' can be used instead.">get_class($object) == 'ChildClass'</warning>,

        /* false-positives: has child classes */
        get_class($object) == 'ParentClass',

        /* false-positives: incomplete classes */
        get_class($object) == '__PHP_Incomplete_Class',
    ];
}

function get_parent_class_cases_holder(\stdClass $object, string $string) {
    return [
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">get_parent_class($object) == 'stdClass'</warning>,
        <warning descr="[EA] '! $object instanceof \stdClass' can be used instead.">get_parent_class($object) != 'stdClass'</warning>,
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">get_parent_class($object) === 'stdClass'</warning>,
        <warning descr="[EA] '! $object instanceof \stdClass' can be used instead.">get_parent_class($object) !== 'stdClass'</warning>,

        <warning descr="[EA] '$object instanceof \FinalClass' can be used instead.">get_parent_class($object) == 'FinalClass'</warning>,
        <warning descr="[EA] '$object instanceof \ChildClass' can be used instead.">get_parent_class($object) == 'ChildClass'</warning>,
        <warning descr="[EA] '$object instanceof \ParentClass' can be used instead.">get_parent_class($object) == 'ParentClass'</warning>,

        /* false-positives: string */
        get_parent_class($string) === 'stdClass',
        get_parent_class($string) == 'FinalClass',
    ];
}

function is_a_cases_holder(\stdClass $object, string $string) {
    return [
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">is_a($object, 'stdClass')</warning>,
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">is_a($object, 'stdClass', false)</warning>,

        <warning descr="[EA] '$object instanceof \FinalClass' can be used instead.">is_a($object, 'FinalClass')</warning>,
        <warning descr="[EA] '$object instanceof \ChildClass' can be used instead.">is_a($object, 'ChildClass')</warning>,
        <warning descr="[EA] '$object instanceof \ParentClass' can be used instead.">is_a($object, 'ParentClass')</warning>,

        /* false-positives: string */
        is_a($string, 'stdClass'),
        is_a($string, 'FinalClass'),
        is_a($object, 'stdClass', true),
        is_a($object, 'FinalClass', true),
    ];
}

function is_subclass_of_cases_holder(\stdClass $object, string $string) {
    return [
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">is_subclass_of($object, 'stdClass')</warning>,
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">is_subclass_of($object, 'stdClass', false)</warning>,

        <warning descr="[EA] '$object instanceof \FinalClass' can be used instead.">is_subclass_of($object, 'FinalClass')</warning>,
        <warning descr="[EA] '$object instanceof \ChildClass' can be used instead.">is_subclass_of($object, 'ChildClass')</warning>,
        <warning descr="[EA] '$object instanceof \ParentClass' can be used instead.">is_subclass_of($object, 'ParentClass')</warning>,

        /* false-positives: string */
        is_subclass_of($string, 'stdClass'),
        is_subclass_of($string, 'FinalClass'),
        is_subclass_of($object, 'stdClass', true),
        is_subclass_of($object, 'FinalClass', true),
    ];
}

function class_implements_cases_holder(\stdClass $object, string $string) {
    return [
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">in_array('stdClass', class_implements($object))</warning>,

        <warning descr="[EA] '$object instanceof \FinalClass' can be used instead.">in_array('FinalClass', class_implements($object))</warning>,
        <warning descr="[EA] '$object instanceof \ChildClass' can be used instead.">in_array('ChildClass', class_implements($object))</warning>,
        <warning descr="[EA] '$object instanceof \ParentClass' can be used instead.">in_array('ParentClass', class_implements($object))</warning>,

        /* false-positives: string */
        in_array('stdClass', class_implements($string)),
        in_array('FinalClass', class_implements($string)),
    ];
}

function class_parents_cases_holder(\stdClass $object, string $string) {
    return [
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">in_array('stdClass', class_parents($object))</warning>,

        <warning descr="[EA] '$object instanceof \FinalClass' can be used instead.">in_array('FinalClass', class_parents($object))</warning>,
        <warning descr="[EA] '$object instanceof \ChildClass' can be used instead.">in_array('ChildClass', class_parents($object))</warning>,
        <warning descr="[EA] '$object instanceof \ParentClass' can be used instead.">in_array('ParentClass', class_parents($object))</warning>,

        /* false-positives: string */
        in_array('stdClass', class_parents($string)),
        in_array('FinalClass', class_parents($string)),
    ];
}