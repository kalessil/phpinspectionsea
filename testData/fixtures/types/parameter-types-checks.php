<?php

    /** @param string[] $stringsArray */
    function arrayTypes (array $array, iterable $iterable, $stringsArray) {
        return
            is_array($array) ||
            is_array($iterable) ||
            is_array($stringsArray) ||

            <warning descr="[EA] Makes no sense, because it's always false according to resolved type. Ensure the parameter is not reused.">is_int($array)</warning> ||
            <warning descr="[EA] Makes no sense, because it's always false according to resolved type. Ensure the parameter is not reused.">is_int($iterable)</warning> ||
            <warning descr="[EA] Makes no sense, because it's always false according to resolved type. Ensure the parameter is not reused.">is_int($stringsArray)</warning> ||

            !<warning descr="[EA] Makes no sense, because it's always true according to resolved type. Ensure the parameter is not reused.">is_int($array)</warning> ||
            !<warning descr="[EA] Makes no sense, because it's always true according to resolved type. Ensure the parameter is not reused.">is_int($iterable)</warning> ||
            !<warning descr="[EA] Makes no sense, because it's always true according to resolved type. Ensure the parameter is not reused.">is_int($stringsArray)</warning>
        ;
    };

    function callableTypes (callable $callable) {
        return [
            is_object($callable),
            is_callable($callable),
            is_string($callable),
            is_array($callable)
        ];
    }

    /**
     *  @param mixed $mixed
     *  @param object $object
     */
    function mixedTypes ($mixed, $object) {
        return [
            is_int($mixed),
            is_int($object)
        ];
    }

    /**
     *  @param float  $float
     *  @param int    $int
     *  @param number $number
     */
    function numericTypes ($float, $int, $number) {
        return [
            is_int($int),
            is_float($float),
            is_int($number), is_float($number)
        ];
    }

    /** @var string $string */
    function stringTypeMagic ($string) {
        $string .= null;
        $string = $string . null;
        return $string;
    }


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

        $second = <warning descr="[EA] New value type (\Clazz) is not matching the resolved parameter type and might introduce types-related false-positives.">$second ?? new Clazz()</warning>;
        $second = <warning descr="[EA] New value type (\Clazz) is not matching the resolved parameter type and might introduce types-related false-positives.">$second ?: new Clazz()</warning>;
        if (null === $second) {
            $second = <warning descr="[EA] New value type (\Clazz) is not matching the resolved parameter type and might introduce types-related false-positives.">new Clazz()</warning>;
        }

        return [ $first, $second ];
    }


    /* case: self/static types support */
    interface IndirectClassReference {
        /** @return self */
        public function returnSelf();
        /** @return static */
        public function returnStatic();
    }
    abstract class IndirectClassReferenceCasesHolder implements IndirectClassReference {
        /** @return static */
        public abstract function ownStatic();
        
        public function fluent_interfaces(IndirectClassReference $one, IndirectClassReference $two, IndirectClassReference $three) {
            $one = $one->returnSelf();
            $one = $one->returnStatic();

            $two = $two->returnSelf();
            $two = $two->returnStatic();
            $two = $two->returnStatic();

            $three = $three->returnSelf();
            $three = $three->returnStatic()->returnStatic();
        }

        public function interface_types(IndirectClassReference $parameter) {
            $parameter = $parameter ?? $this->returnSelf();
            $parameter = $this->returnSelf();
            $parameter = $this->returnSelf()->returnSelf();

            $parameter = $parameter ?? $this->returnStatic();
            $parameter = $this->returnStatic();
            $parameter = $this->returnStatic()->returnStatic();

            $parameter = <warning descr="[EA] New value type (null) is not matching the resolved parameter type and might introduce types-related false-positives.">null</warning>;
        }

        public function class_types(IndirectClassReference $parameter) {
            if ($parameter instanceof IndirectClassReferenceTest) {
                $parameter = $parameter->ownStatic();
            }
        }
    }

    function class_types(IndirectClassReference $parameter) {
        if ($parameter instanceof IndirectClassReferenceTest) {
            $parameter = $parameter->ownStatic();
        }
    }

    /* false-positive: incomplete types influenced by null as default value */
    function incomplete_types($one = null, $two = []) {
        $one = '';
        $two = <warning descr="[EA] New value type (string) is not matching the resolved parameter type and might introduce types-related false-positives.">''</warning>;
    }

    /* false-positive: core functions returning string|false, string|null */
    function core_api_functions_consistency(string $string, string $replace, string $regex, array $replaces, array $regexes) {
        $string = substr($string, -1);

        $replace = str_replace('', '', $replace);
        $regex = preg_replace('', '', $regex);

        $replaces = str_replace('', '', $replaces);
        $regexes = preg_replace('', '', $regexes);
    }
    function core_api_functions_consistency_side_effects(string $string) {
        $string = str_replace('...', '...', $string);
        $string = str_replace('...', '...', $string);
    }

    /* false-positive: nullable objects */
    function returns_nullable_object(): ?stdClass {}
    function assigning_nullable_objects(stdClass $object) {
        $object = returns_nullable_object();
    }

    /* false-positive: nullable array */
    /** @return array */
    function returns_array() { return []; }
    /** @param $arrayAnnotated array|null */
    function assigning_nullable_array(array &$parameter, array $array = null, array $arrayAnnotated = null) {
        $parameter = $array ?? returns_array();
        $parameter = ($array ?? returns_array());
        $parameter = ($arrayAnnotated ?? returns_array());
    }

    /* false-positive: iterable */
    function iterable_support(iterable $iterable = null) {
        $iterable = $iterable ?? [];
    }

    /* false-positives: issues with proper binary expression types identification */
    function binary_expression_types(int $parameter, int $lifetime) {
        $parameter = 1 * $parameter * 1;
        $parameter = <warning descr="[EA] New value type (float) is not matching the resolved parameter type and might introduce types-related false-positives.">1 * 1.0 * $parameter * 1</warning>;

        $rounded  = (int) (ceil(time() / $lifetime) * $lifetime);
        $lifetime = $rounded - time();
    }

    /* false-positives: parse_url behaviour */
    function parse_url_behaviour(string $string, int $int, array $array) {
        $array  = <warning descr="[EA] New value type (bool) is not matching the resolved parameter type and might introduce types-related false-positives.">parse_url($string) ?: []</warning>;
        $int    = parse_url($string, PHP_URL_PORT) ?: 80;
        $string = parse_url($string, PHP_URL_PATH) ?: '/';
    }