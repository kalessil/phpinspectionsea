<?php

final class <warning descr="Utility class's name should end with 'Util'.">EntityHelper</warning>
{
    private function __construct()       {}
    static public function serialize()   {}
    static public function unserialize() {}
}

/* false-positives: commonly used factory methods and non-static methods */
final class EntityHelperValid1
{
    private function __construct()       {}
    static public function unserialize() {}
    public function getIterator()        {} // <- disables the case reporting
}
final class EntityHelperValid2
{
    private function __construct()       {}
    static public function unserialize() {}
    static public function createFrom()  {} // <- disables the case reporting
}
final class EntityHelperValid3
{
    private function __construct()       {}
    static public function unserialize() {}
    static public function valueOf()     {} // <- disables the case reporting
}

/* false-positives: commonly used factory method as the single static method */
final class EntityHelperValid4
{
    private function __construct()       {}
    static public function valueOf()     {} // <- disables the case reporting
}