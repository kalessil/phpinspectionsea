<?php

interface IWithToString                { public function __toString(); }
interface IWithoutToString             {}
abstract class AbstractWithToString    { abstract public function __toString(); }
abstract class AbstractWithoutToString {}
class ClassWithToString                { public function __toString() {} }
class ClassWithoutToString             {}

function checkInterfaces(IWithToString $i1, IWithoutToString $i2) {
    if (
        <weak_warning descr="''' != $i1' can be used instead.">strlen($i1)</weak_warning> ||
        <error descr="\IWithoutToString miss __toString() implementation.">strlen($i2)</error>
    ) {}
}
function checkAbstractClasses(AbstractWithToString $a1, AbstractWithoutToString $a2) {
    if (
        <weak_warning descr="''' != $a1' can be used instead.">strlen($a1)</weak_warning> ||
        <error descr="\AbstractWithoutToString miss __toString() implementation.">strlen($a2)</error>
    ) {}
}
function checkClasses(ClassWithToString $c1, ClassWithoutToString $c2) {
    if (
        <weak_warning descr="''' != $c1' can be used instead.">strlen($c1)</weak_warning> ||
        <error descr="\ClassWithoutToString miss __toString() implementation.">strlen($c2)</error>
    ) {}
}

function checkUseCases($strToTest) {
    if (<weak_warning descr="''' !== $strToTest' can be used instead.">mb_strlen($strToTest)</weak_warning>)  {}
    if (<weak_warning descr="''' === $strToTest' can be used instead.">!mb_strlen($strToTest)</weak_warning>) {}
    if (<weak_warning descr="''' !== $strToTest' can be used instead.">strlen($strToTest)</weak_warning>)     {}
    if (<weak_warning descr="''' === $strToTest' can be used instead.">!strlen($strToTest)</weak_warning>)    {}

    if ($strToTest || <weak_warning descr="''' !== $strToTest' can be used instead.">strlen($strToTest)</weak_warning>) {}
    if ($strToTest && <weak_warning descr="''' !== $strToTest' can be used instead.">strlen($strToTest)</weak_warning>) {}

    if (<weak_warning descr="''' === $strToTest' can be used instead.">strlen($strToTest) == 0</weak_warning>)  {}
    if (<weak_warning descr="''' !== $strToTest' can be used instead.">strlen($strToTest) != 0</weak_warning>)  {}
    if (<weak_warning descr="''' === $strToTest' can be used instead.">strlen($strToTest) === 0</weak_warning>) {}
    if (<weak_warning descr="''' !== $strToTest' can be used instead.">strlen($strToTest) !== 0</weak_warning>) {}

    if (<weak_warning descr="''' === $strToTest' can be used instead.">0 ==  strlen($strToTest)</weak_warning>) {}
    if (<weak_warning descr="''' !== $strToTest' can be used instead.">0 !=  strlen($strToTest)</weak_warning>) {}
    if (<weak_warning descr="''' === $strToTest' can be used instead.">0 === strlen($strToTest)</weak_warning>) {}
    if (<weak_warning descr="''' !== $strToTest' can be used instead.">0 !== strlen($strToTest)</weak_warning>) {}

    if (<weak_warning descr="''' === $strToTest' can be used instead.">strlen($strToTest) < 1</weak_warning>)   {}
    if (<weak_warning descr="''' !== $strToTest' can be used instead.">strlen($strToTest) >= 1</weak_warning>)  {}
    if (<weak_warning descr="''' !== $strToTest' can be used instead.">strlen($strToTest) > 0</weak_warning>)   {}

    /* not yet supported */
    if (1 >  strlen($strToTest))   {}
    if (1 <= strlen($strToTest))  {}
}