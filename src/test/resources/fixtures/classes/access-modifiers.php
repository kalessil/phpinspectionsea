<?php

class classAM1
{
    static <error descr="static should be declared with access modifier.">$static</error>;
    var    <error descr="member should be declared with access modifier.">$member</error>;
    function <error descr="defaultVisibilityMethod should be declared with access modifier.">defaultVisibilityMethod</error> () {}

    public $publicProperty;
    public function publicVisibilityMethod()       {}
    protected function protectedVisibilityMethod() {}
    private function privateVisibilityMethod()     {}
}

abstract class classAM2
{
    static   function <error descr="defaultVisibilityStaticMethod should be declared with access modifier.">defaultVisibilityStaticMethod</error> () {}
    final    function <error descr="defaultVisibilityFinalMethod should be declared with access modifier.">defaultVisibilityFinalMethod</error> ()  {}
    abstract function <error descr="defaultVisibilityAbstractMethod should be declared with access modifier.">defaultVisibilityAbstractMethod</error> ();
}

interface interfaceAM1
{
    function <error descr="defaultVisibilityMethod should be declared with access modifier.">defaultVisibilityMethod</error> ();
}

trait traitAM1
{
    function <error descr="defaultVisibilityMethod should be declared with access modifier.">defaultVisibilityMethod</error> () {}
}