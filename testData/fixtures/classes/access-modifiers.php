<?php

class classAM1
{
    static <weak_warning descr="[EA] 'static' should be declared with access modifier.">$static</weak_warning>;
    var    <weak_warning descr="[EA] 'member' should be declared with access modifier.">$member</weak_warning>;
    function <weak_warning descr="[EA] 'defaultVisibilityMethod' should be declared with access modifier.">defaultVisibilityMethod</weak_warning> () {}

    const     A_CONSTANT = '';
    public    $publicProperty;
    protected $protectedProperty;
    private   $privateProperty;

    public function publicVisibilityMethod()       {}
    protected function protectedVisibilityMethod() {}
    private function privateVisibilityMethod()     {}
}

abstract class classAM2
{
    static function <weak_warning descr="[EA] 'defaultVisibilityStaticMethod' should be declared with access modifier.">defaultVisibilityStaticMethod</weak_warning> () {}
    final function <weak_warning descr="[EA] 'defaultVisibilityFinalMethod' should be declared with access modifier.">defaultVisibilityFinalMethod</weak_warning> ()  {}
    abstract function <weak_warning descr="[EA] 'defaultVisibilityAbstractMethod' should be declared with access modifier.">defaultVisibilityAbstractMethod</weak_warning> ();
}

interface interfaceAM1
{
    function <weak_warning descr="[EA] 'defaultVisibilityMethod' should be declared with access modifier.">defaultVisibilityMethod</weak_warning> ();
}

trait traitAM1
{
    function <weak_warning descr="[EA] 'defaultVisibilityMethod' should be declared with access modifier.">defaultVisibilityMethod</weak_warning> () {}
}
