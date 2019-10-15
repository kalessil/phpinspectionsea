<?php

final class FinalClass               {}

class ParentClass                    {}
class ChildClass extends ParentClass {}

/** @param mixed $mixed */
function get_class_cases_holder(\stdClass $object, string $string, $mixed) {
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

        /* false-positives: string and mixed */
        get_class($string) === 'stdClass',
        get_class($string) == 'FinalClass',
        get_class($mixed) === 'stdClass',
        get_class($mixed) == 'FinalClass',
    ];
}

/** @param mixed $mixed */
function get_parent_class_cases_holder(\stdClass $object, string $string, $mixed) {
    return [
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">get_parent_class($object) == 'stdClass'</warning>,
        <warning descr="[EA] '! $object instanceof \stdClass' can be used instead.">get_parent_class($object) != 'stdClass'</warning>,
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">get_parent_class($object) === 'stdClass'</warning>,
        <warning descr="[EA] '! $object instanceof \stdClass' can be used instead.">get_parent_class($object) !== 'stdClass'</warning>,

        <warning descr="[EA] '$object instanceof \FinalClass' can be used instead.">get_parent_class($object) == 'FinalClass'</warning>,
        <warning descr="[EA] '$object instanceof \ChildClass' can be used instead.">get_parent_class($object) == 'ChildClass'</warning>,
        <warning descr="[EA] '$object instanceof \ParentClass' can be used instead.">get_parent_class($object) == 'ParentClass'</warning>,

        /* false-positives: string and mixed */
        get_parent_class($string) === 'stdClass',
        get_parent_class($string) == 'FinalClass',
        get_parent_class($string) === '$mixed',
        get_parent_class($string) == '$mixed',
    ];
}

/** @param mixed $mixed */
function is_a_cases_holder(\stdClass $object, string $string, $mixed) {
    return [
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">is_a($object, 'stdClass')</warning>,
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">is_a($object, 'stdClass', false)</warning>,

        <warning descr="[EA] '$object instanceof \FinalClass' can be used instead.">is_a($object, 'FinalClass')</warning>,
        <warning descr="[EA] '$object instanceof \ChildClass' can be used instead.">is_a($object, 'ChildClass')</warning>,
        <warning descr="[EA] '$object instanceof \ParentClass' can be used instead.">is_a($object, 'ParentClass')</warning>,

        /* false-positives: string and mixed */
        is_a($string, 'stdClass'),
        is_a($string, 'FinalClass'),
        is_a($mixed, 'stdClass'),
        is_a($mixed, 'FinalClass'),
        is_a($object, 'stdClass', true),
        is_a($object, 'FinalClass', true),
    ];
}

/** @param mixed $mixed */
function is_subclass_of_cases_holder(\stdClass $object, string $string, $mixed) {
    return [
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">is_subclass_of($object, 'stdClass')</warning>,
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">is_subclass_of($object, 'stdClass', false)</warning>,

        <warning descr="[EA] '$object instanceof \FinalClass' can be used instead.">is_subclass_of($object, 'FinalClass')</warning>,
        <warning descr="[EA] '$object instanceof \ChildClass' can be used instead.">is_subclass_of($object, 'ChildClass')</warning>,
        <warning descr="[EA] '$object instanceof \ParentClass' can be used instead.">is_subclass_of($object, 'ParentClass')</warning>,

        /* false-positives: string and mixed */
        is_subclass_of($string, 'stdClass'),
        is_subclass_of($string, 'FinalClass'),
        is_subclass_of($mixed, 'stdClass'),
        is_subclass_of($mixed, 'FinalClass'),
        is_subclass_of($object, 'stdClass', true),
        is_subclass_of($object, 'FinalClass', true),
    ];
}

/** @param mixed $mixed */
function class_implements_cases_holder(\stdClass $object, string $string, $mixed) {
    return [
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">in_array('stdClass', class_implements($object))</warning>,

        <warning descr="[EA] '$object instanceof \FinalClass' can be used instead.">in_array('FinalClass', class_implements($object))</warning>,
        <warning descr="[EA] '$object instanceof \ChildClass' can be used instead.">in_array('ChildClass', class_implements($object))</warning>,
        <warning descr="[EA] '$object instanceof \ParentClass' can be used instead.">in_array('ParentClass', class_implements($object))</warning>,

        /* false-positives: string and mixed */
        in_array('stdClass', class_implements($string)),
        in_array('FinalClass', class_implements($string)),
        in_array('stdClass', class_implements($mixed)),
        in_array('FinalClass', class_implements($mixed)),
    ];
}

/** @param mixed $mixed */
function class_parents_cases_holder(\stdClass $object, string $string, $mixed) {
    return [
        <warning descr="[EA] '$object instanceof \stdClass' can be used instead.">in_array('stdClass', class_parents($object))</warning>,

        <warning descr="[EA] '$object instanceof \FinalClass' can be used instead.">in_array('FinalClass', class_parents($object))</warning>,
        <warning descr="[EA] '$object instanceof \ChildClass' can be used instead.">in_array('ChildClass', class_parents($object))</warning>,
        <warning descr="[EA] '$object instanceof \ParentClass' can be used instead.">in_array('ParentClass', class_parents($object))</warning>,

        /* false-positives: string and mixed */
        in_array('stdClass', class_parents($string)),
        in_array('FinalClass', class_parents($string)),
        in_array('stdClass', class_parents($mixed)),
        in_array('FinalClass', class_parents($mixed)),
    ];
}