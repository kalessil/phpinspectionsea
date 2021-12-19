<?php

    interface IWithToString                { public function __toString(); }
    interface IWithoutToString             {}
    abstract class AbstractWithToString    { abstract public function __toString(); }
    abstract class AbstractWithoutToString {}
    class ClassWithToString                { public function __toString() {} }
    class ClassWithoutToString             {}

    function check_interfaces(IWithToString $i1, IWithoutToString $i2) {
        return [
            $i1 != '',
            $i2 != '',
        ];
    }
    function check_abstract_classes(AbstractWithToString $a1, AbstractWithoutToString $a2) {
        return [
            $a1 != '',
            $a2 != '',
        ];
    }
    function check_implementation_classes(ClassWithToString $c1, ClassWithoutToString $c2) {
        return [
            $c1 != '',
            $c2 != '',
        ];
    }

    class ClassNeedsToStringMethod {}

    /* pattern: object can not be used in string context */
    $object = new ClassNeedsToStringMethod();
    $result = $object == '...';
    $result = $object != '...';
    $result = $object <> '...';

    /* pattern: safe comparison */
    $result = $x === '...';
    $result = $x !== '...';
    $result = $x !== '...';

    /* pattern: needs hardening */
    $result = $x == '';
    $result = $x != '';
    $result = $x == '0';
    $result = $x != '0';
    $result = $x == $y;
    $result = $x != $y;
    $result = $x == '0.0';
    $result = $x == '.0';
    $result = $x == '-0';
    $result = $x == '+0';