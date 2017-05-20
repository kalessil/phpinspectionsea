<?php

abstract class MyClass {
    // Alloweds.
    function onlyFunction() {}
    public function publicFunction() {}
    public static function publicStaticFunction() { }
    abstract public function abstractFunction();
    abstract function abstractOnlyFunction();
    static function staticOnlyFunction() { }
    abstract public static function abstractPublicStaticFunction();

    public
    static function publicStaticFunctionMultiline() { }

    // Should warn.
    <weak_warning descr="Missorted modifiers 'static public'">static public</weak_warning> function staticPublicFunction() { }

    <weak_warning descr="Missorted modifiers 'public abstract'">public abstract</weak_warning> function publicAbstractFunction();

    <weak_warning descr="Missorted modifiers 'static public'">static
    public</weak_warning> function staticPublicFunctionMultiline() { }

    <weak_warning descr="Missorted modifiers 'abstract static public'">abstract static public</weak_warning> function abstractStaticPublicFunction();

    <weak_warning descr="Missorted modifiers 'static abstract public'">static abstract public</weak_warning> function staticAbstractPublicFunction();

    <weak_warning descr="Missorted modifiers 'static public abstract'">static public abstract</weak_warning> function staticPublicAbstractFunction();
}
