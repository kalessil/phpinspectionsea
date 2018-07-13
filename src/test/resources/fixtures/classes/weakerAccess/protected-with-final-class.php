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
    <weak_warning descr="Since the class is final, the member can be declared private.">protected</weak_warning> $isProtected;
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
    <weak_warning descr="Since the class is final, the member can be declared private.">protected</weak_warning> function protectedMethod() { }
    private   function privateMethod() { }

    static           function staticNoModifierMethod() { }
    static public    function staticPublicMethod() { }
    static <weak_warning descr="Since the class is final, the member can be declared private.">protected</weak_warning> function staticProtectedMethod() { }
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
    <weak_warning descr="Since the class is final, the member can be declared private.">protected</weak_warning> const PROTECTED_CONSTANT  = 2;
    private const PRIVATE_CONSTANT     = 3;
}

/* applicable: should affect the protected constant, even with different case */
final class FinalClassWithUppercaseConstant {
    <weak_warning descr="Since the class is final, the member can be declared private.">PROTECTED</weak_warning> const IGNORE_CASE = true;
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
    <weak_warning descr="Since the class is final, the member can be declared private.">protected</weak_warning> $ownProtectedField;
    <weak_warning descr="Since the class is final, the member can be declared private.">protected</weak_warning> function ownProtectedMethod() { }
}