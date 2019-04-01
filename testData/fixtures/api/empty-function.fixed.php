<?php

    /* pattern: count can be used */
    echo count([]) === 0;
    echo count([]) !== 0;

    /**
     * @param int|null $int
     * @param float|null $float
     * @param bool|null $boolean
     * @param resource|null $resource
     * @param null|string $string
     */
    function empty_with_typed_params($int, $float, $boolean, $resource, $string) {
        return [
            /* pattern: can be compared to null */
            $int === null,
            $float === null,
            $boolean === null,
            $resource === null,
            empty($string),
        ];
    }

    function nullable_int(?int $int) { return $int; }
    echo nullable_int() === null;
    echo nullable_int() !== null;

    function nullable_string(?string $string) { return $string; }
    echo empty(nullable_string());
    echo !empty(nullable_string());

    echo empty(1);
    echo empty('...');
    echo empty(null);

    abstract class ClassForEmptyWithFields {
        /** @var string|null */
        public $string;
        /** @var ClassForEmptyWithFields|null */
        public $field;
        /** @var ClassForEmptyWithFields[]|null */
        public $fields;
        /** @return ClassForEmptyWithFields|null */
        abstract function get();
    }
    function empty_with_fields(ClassForEmptyWithFields $subject) {
        return [
            empty($subject->string),
            empty($subject->field),
            empty($subject->field->field),
            empty($subject->fields[0]->field),
            empty($subject->get()->field),
        ];
    }
