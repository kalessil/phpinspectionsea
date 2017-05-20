<?php

abstract class MyClass {
    // Alloweds.
    function onlyFunction() {}
    public function publicFunction() {}
    public static function publicStaticFunction() { }
    abstract public function abstractFunction();
    abstract function abstractOnlyFunction();
    static function staticOnlyFunction() { }

    public
    static function publicStaticFunctionMultiline() { }

    // Should warn.
    <weak_warning descr="Missorted modifiers 'static public'">static public</weak_warning> function staticPublicFunction() { }

    <weak_warning descr="Missorted modifiers 'public abstract'">public abstract</weak_warning> function publicAbstractFunction();

    <weak_warning descr="Missorted modifiers 'static public'">static
    public</weak_warning> function staticPublicFunctionMultiline() { }
}
