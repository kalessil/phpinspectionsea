<?php

// Not appliable (not is a final class).
class NoFinalClassWithProperties {
    public    $isPublic;
    protected $isProtected;
    private   $isPrivate;
}

// Appliable: should affect only the protected property.
final class FinalClasWithProperties {
    public  $isPublic;
    private $isProtected;
    private $isPrivate;
}

// Not appliable (not is a final class).
class NoFinalClassWithMethods {
              function noModifierMethod() { }
    public    function publicMethod() { }
    protected function protectedMethod() { }
    private   function privateMethod() { }

    static           function staticNoModifierMethod() { }
    static public    function staticPublicMethod() { }
    static protected function staticProtectedMethod() { }
    static private   function staticPrivateMethod() { }
}

// Not appliable (not is a final class).
abstract class AbstractClass {
    abstract           function abstractNoModifierMethod();
    abstract public    function abstractPublicMethod();
    abstract protected function abstractProtectedMethod();
}

// Appliable: should affect both protected methods.
final class FinalClassWithMethods {
              function noModifierMethod() { }
    public    function publicMethod() { }
    private function protectedMethod() { }
    private   function privateMethod() { }

    static           function staticNoModifierMethod() { }
    static public    function staticPublicMethod() { }
    static private function staticProtectedMethod() { }
    static private   function staticPrivateMethod() { }
}

// Not appliable.
class ClassWithConstant {
              const NOMODIFIER_CONSTANT = 0;
    public    const PUBLIC_CONSTANT     = 1;
    protected const PROTECTED_CONSTANT  = 2;
    private   const PRIVATE_CONSTANT    = 3;
}

// Appliable: should affect only the protected constant.
final class FinalClassWithConstant {
            const NOMODIFIER_CONSTANT = 0;
    public  const PUBLIC_CONSTANT     = 1;
    private const PROTECTED_CONSTANT  = 2;
    private const PRIVATE_CONSTANT    = 3;
}

// Appliable: should affect the protected constant, even with different case.
final class FinalClassWithUppercaseConstant {
    private const IGNORE_CASE = true;
}

// Not appliable.
abstract class AbstractClassTestingAllTypes {
    protected const PROTECTED_CONTANT = 0;

    protected $isProtected;

    abstract protected function abstractProtectedMethod();
             protected function protectedMethod() { }
}

// Not appliable: even final class should respect the parent signature.
final class RespectAbstractClassSignatures extends AbstractClassTestingAllTypes {
    protected const PROTECTED_CONTANT = 0;

    protected $isProtected;

    protected function abstractProtectedMethod() { }
    protected function protectedMethod() { }
}
