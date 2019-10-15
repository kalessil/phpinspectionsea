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
            <error descr="[EA] \IWithoutToString miss __toString() implementation.">$i2 != ''</error>,
        ];
    }
    function check_abstract_classes(AbstractWithToString $a1, AbstractWithoutToString $a2) {
        return [
            $a1 != '',
            <error descr="[EA] \AbstractWithoutToString miss __toString() implementation.">$a2 != ''</error>,
        ];
    }
    function check_implementation_classes(ClassWithToString $c1, ClassWithoutToString $c2) {
        return [
            $c1 != '',
            <error descr="[EA] \ClassWithoutToString miss __toString() implementation.">$c2 != ''</error>,
        ];
    }

    class ClassNeedsToStringMethod {}

    /* pattern: object can not be used in string context */
    $object = new ClassNeedsToStringMethod();
    $result = <error descr="[EA] \ClassNeedsToStringMethod miss __toString() implementation.">$object == '...'</error>;
    $result = <error descr="[EA] \ClassNeedsToStringMethod miss __toString() implementation.">$object != '...'</error>;
    $result = <error descr="[EA] \ClassNeedsToStringMethod miss __toString() implementation.">$object <> '...'</error>;

    /* pattern: safe comparison */
    $result = <warning descr="[EA] Safely use '===' here.">$x == '...'</warning>;
    $result = <warning descr="[EA] Safely use '!==' here.">$x != '...'</warning>;
    $result = <warning descr="[EA] Safely use '!==' here.">$x <> '...'</warning>;

    /* pattern: needs hardening */
    $result = <weak_warning descr="[EA] Please consider using more strict '===' here (hidden types casting will not be applied anymore).">$x == ''</weak_warning>;
    $result = <weak_warning descr="[EA] Please consider using more strict '!==' here (hidden types casting will not be applied anymore).">$x != ''</weak_warning>;
    $result = <weak_warning descr="[EA] Please consider using more strict '===' here (hidden types casting will not be applied anymore).">$x == '0'</weak_warning>;
    $result = <weak_warning descr="[EA] Please consider using more strict '!==' here (hidden types casting will not be applied anymore).">$x != '0'</weak_warning>;
    $result = <weak_warning descr="[EA] Please consider using more strict '===' here (hidden types casting will not be applied anymore).">$x == $y</weak_warning>;
    $result = <weak_warning descr="[EA] Please consider using more strict '!==' here (hidden types casting will not be applied anymore).">$x != $y</weak_warning>;