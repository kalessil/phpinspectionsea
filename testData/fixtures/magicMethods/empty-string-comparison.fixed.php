<?php

interface IWithToString                { public function __toString(); }
interface IWithoutToString             {}
abstract class AbstractWithToString    { abstract public function __toString(); }
abstract class AbstractWithoutToString {}
class ClassWithToString                { public function __toString() {} }
class ClassWithoutToString             {}

function checkInterfaces(IWithToString $i1, IWithoutToString $i2) {
    if (
        '' != $i1 ||
        strlen($i2)
    ) {}
}
function checkAbstractClasses(AbstractWithToString $a1, AbstractWithoutToString $a2) {
    if (
        '' != $a1 ||
        strlen($a2)
    ) {}
}
function checkClasses(ClassWithToString $c1, ClassWithoutToString $c2) {
    if (
        '' != $c1 ||
        strlen($c2)
    ) {}
}

function checkUseCases($strToTest) {
    if ('' !== $strToTest)  {}
    if ('' === $strToTest) {}
    if ('' !== $strToTest)     {}
    if ('' === $strToTest)    {}

    if ($strToTest || '' !== $strToTest) {}
    if ($strToTest && '' !== $strToTest) {}

    if ('' === $strToTest)  {}
    if ('' !== $strToTest)  {}
    if ('' === $strToTest) {}
    if ('' !== $strToTest) {}

    if ('' === $strToTest) {}
    if ('' !== $strToTest) {}
    if ('' === $strToTest) {}
    if ('' !== $strToTest) {}

    if ('' === $strToTest)   {}
    if ('' !== $strToTest)  {}

    /* not yet supported */
    if (1 >  strlen($strToTest))   {}
    if (1 <= strlen($strToTest))  {}
}