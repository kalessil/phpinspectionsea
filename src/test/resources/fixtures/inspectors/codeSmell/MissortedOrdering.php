<?php

abstract class MyClass {
    // Alloweds.
    public function publicFunction() {}
    public static function publicStaticFunction() { }
    abstract public function abstractFunction();

    // Case #1
    <weak_warning descr="Missorted modifiers 'static public'">static public</weak_warning> function staticPublicFunction() { }

    // Case #2
    <weak_warning descr="Missorted modifiers 'public abstract'">public abstract</weak_warning> function publicAbstractFunction();
}
