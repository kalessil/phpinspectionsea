<?php

interface IWithToString    { public function __toString(); }
interface IWithoutToString {}

abstract class AbstractWithToString    { abstract public function __toString(); }
abstract class AbstractWithoutToString {}

class ClassWithToString extends ClassWithoutToString { public function __toString() { return ''; } }
class ClassWithoutToString                           { }

function checkInterfaces(IWithToString $i1, IWithoutToString $i2) {
    if (strlen($i1) || strlen($i2)) { return; }
}
function checkAbstractClasses(AbstractWithToString $a1, AbstractWithoutToString $a2) {
    if (strlen($a1) || strlen($a2)) { return; }
}
function checkClasses(ClassWithToString $c1, ClassWithoutToString $c2) {
    if (strlen($c1) || strlen($c2)) { return; }
}

function checkUseCases($strToTest) {
    if (strlen($strToTest))  { return; }
    if (!strlen($strToTest)) { return; }

    if ($strToTest || strlen($strToTest)) { return; }
    if ($strToTest && strlen($strToTest)) { return; }

    if (strlen($strToTest) == 0)  { return; }
    if (strlen($strToTest) != 0)  { return; }
    if (strlen($strToTest) === 0) { return; }
    if (strlen($strToTest) !== 0) { return; }

    if (0 ==  strlen($strToTest)) { return; }
    if (0 !=  strlen($strToTest)) { return; }
    if (0 === strlen($strToTest)) { return; }
    if (0 !== strlen($strToTest)) { return; }

    if (strlen($strToTest) < 1)   { return; }
    if (strlen($strToTest) >= 1)  { return; }

    /* we'll not support this */
    if (1 >  strlen($strToTest))   { return; }
    if (1 <= strlen($strToTest))  { return; }
}