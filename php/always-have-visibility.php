<?php

abstract class x {
    static $staticProperty;
    var    $varProperty;
    function defaultVisibilityMethod()                 {}
    static   function defaultVisibilityStaticMethod()  {}
    final    function defaultVisibilityFinalMethod()   {}
    abstract function defaultVisibilityAbstractMethod();


    public $publicProperty;
    public function publicVisibilityMethod()           {}

}