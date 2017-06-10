<?php

interface MyInterface
{
    // Alloweds.
    public static function publicStaticFunction();

    // Should warn.
    <weak_warning descr="Misordered modifiers 'static public'">static public</weak_warning> function staticPublicFunction();
}

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
    <weak_warning descr="Misordered modifiers 'static public'">static public</weak_warning> function staticPublicFunction() { }

    <weak_warning descr="Misordered modifiers 'public abstract'">public abstract</weak_warning> function publicAbstractFunction();

    <weak_warning descr="Misordered modifiers 'static public'">static
    public</weak_warning> function staticPublicFunctionMultiline() { }

    <weak_warning descr="Misordered modifiers 'abstract static public'">abstract static public</weak_warning> function abstractStaticPublicFunction();

    <weak_warning descr="Misordered modifiers 'static abstract public'">static abstract public</weak_warning> function staticAbstractPublicFunction();

    <weak_warning descr="Misordered modifiers 'static public abstract'">static public abstract</weak_warning> function staticPublicAbstractFunction();
}

class MyAnotherClass {
    // Alloweds.
    final public static function finalPublicStaticFunction() { }

    // Should warn.
    final static function finalStaticFunction() { }

    <weak_warning descr="Misordered modifiers 'static final'">static final</weak_warning> function staticFinalFunction() { }

    <weak_warning descr="Misordered modifiers 'static public final'">static public final</weak_warning> function staticPublicFinalFunction() { }

    <weak_warning descr="Misordered modifiers 'static public final'">STATIC PUBLIC FINAL</weak_warning> function staticPublicFinalFunctionUppercased() { }
}
