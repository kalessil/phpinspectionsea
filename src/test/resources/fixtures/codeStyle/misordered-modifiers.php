<?php

interface MyInterface
{
    // Alloweds.
    public static function publicStaticFunction();

    // Should warn.
    <weak_warning descr="Modifiers are misordered (according to PSRs)">static public</weak_warning> function staticPublicFunction();
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
    <weak_warning descr="Modifiers are misordered (according to PSRs)">static public</weak_warning> function staticPublicFunction() { }

    <weak_warning descr="Modifiers are misordered (according to PSRs)">public abstract</weak_warning> function publicAbstractFunction();

    <weak_warning descr="Modifiers are misordered (according to PSRs)">static
    public</weak_warning> function staticPublicFunctionMultiline() { }

    <weak_warning descr="Modifiers are misordered (according to PSRs)">abstract static public</weak_warning> function abstractStaticPublicFunction();

    <weak_warning descr="Modifiers are misordered (according to PSRs)">static abstract public</weak_warning> function staticAbstractPublicFunction();

    <weak_warning descr="Modifiers are misordered (according to PSRs)">static public abstract</weak_warning> function staticPublicAbstractFunction();
}

class MyAnotherClass {
    // Alloweds.
    final public static function finalPublicStaticFunction() { }

    // Should warn.
    final static function finalStaticFunction() { }

    <weak_warning descr="Modifiers are misordered (according to PSRs)">static final</weak_warning> function staticFinalFunction() { }

    <weak_warning descr="Modifiers are misordered (according to PSRs)">static public final</weak_warning> function staticPublicFinalFunction() { }

    <weak_warning descr="Modifiers are misordered (according to PSRs)">STATIC PUBLIC FINAL</weak_warning> function staticPublicFinalFunctionUppercased() { }
}
