<?php

class classAM1
{
    public static $static;
    public $member;
    public function defaultVisibilityMethod () {}

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
    public static function defaultVisibilityStaticMethod () {}
    public final function defaultVisibilityFinalMethod ()  {}
    public abstract function defaultVisibilityAbstractMethod ();
}

interface interfaceAM1
{
    public function defaultVisibilityMethod ();
}

trait traitAM1
{
    public function defaultVisibilityMethod () {}
}