<?php

class <weak_warning descr="[EA] Properties 'property1' and 'property2' naming is quite similar, consider renaming one for avoiding misuse.">ClazzWithProperties</weak_warning> {
    private $property1;
    private $property2;

    private $propertyForCache;
    private $propertyForObject;
    private $propertyForObjects;
}

class <weak_warning descr="[EA] Methods 'method1' and 'method2' naming is quite similar, consider renaming one for avoiding misuse.">ClazzWithMethods</weak_warning> {
    private function method1($required)                   {}
    private function method2($required, $optional = null) {}

    private function setObject()  {}
    private function getObject()  {}
    private function getObjects() {}
}