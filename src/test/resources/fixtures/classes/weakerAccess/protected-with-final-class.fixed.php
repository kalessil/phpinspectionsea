<?php

/* not applicable: not is a final class */
class NoFinalClassWithProperties {
    public    $isPublic;
    protected $isProtected;
    private   $isPrivate;
}

/* applicable: should affect only the protected property */
final class FinalClassWithProperties {
    public  $isPublic;
    private $isProtected;
    private $isPrivate;
}

/* not applicable: not is a final class */
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

/* not applicable: not is a final class */
abstract class AbstractClass {
    abstract           function abstractNoModifierMethod();
    abstract public    function abstractPublicMethod();
    abstract protected function abstractProtectedMethod();
}

/* applicable: should affect both protected methods */
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

/* not applicable: not a final class */
class ClassWithConstant {
              const NO_MODIFIER_CONSTANT = 0;
    public    const PUBLIC_CONSTANT      = 1;
    protected const PROTECTED_CONSTANT   = 2;
    private   const PRIVATE_CONSTANT     = 3;
}

/* applicable: should affect only the protected constant */
final class FinalClassWithConstant {
            const NO_MODIFIER_CONSTANT = 0;
    public  const PUBLIC_CONSTANT      = 1;
    private const PROTECTED_CONSTANT  = 2;
    private const PRIVATE_CONSTANT     = 3;
}

/* applicable: should affect the protected constant, even with different case */
final class FinalClassWithUppercaseConstant {
    private const IGNORE_CASE = true;
}

/* not applicable: not a final class */
abstract class AbstractClassTestingAllTypes {
    protected const PROTECTED_CONSTANT = 0;
    protected $isProtected;
    abstract protected function abstractProtectedMethod();
             protected function protectedMethod() { }
}

/* not applicable: even final class should respect the parent signature */
final class RespectAbstractClassSignatures extends AbstractClassTestingAllTypes {
    protected const PROTECTED_CONSTANT = 0;
    protected $isProtected;
    protected function abstractProtectedMethod() { }
    protected function protectedMethod() { }
}

/* applicable: protected members/references which are not presented in parent class */
final class FinalClassWithNonOverriddenProtectedMembers extends stdClass {
    private $ownProtectedField;
    private function ownProtectedMethod() { }
}