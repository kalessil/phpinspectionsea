<?php

/* base classes for the test case*/
class ClassOverridesField1 {
    private   $privateFrom1;
    protected $protectedFrom1;
    protected $weakened;

    static protected $staticFrom1;
    const CLAZZ = __CLASS__;
}
class ClassOverridesField2 extends ClassOverridesField1 {
    private   $privateFrom2;
    protected $protectedFrom2;
}


/* Test-cases */
/** @property $privateFrom2 */
class ClassOverridesField extends ClassOverridesField2 {
    /* this suggested to be re-initialized in constructor */
    <weak_warning descr="Field 'protectedFrom1' is already defined in \ClassOverridesField1, check our online documentation for options.">protected $protectedFrom1;</weak_warning>
    <weak_warning descr="Field 'protectedFrom2' is already defined in \ClassOverridesField2, check our online documentation for options.">protected $protectedFrom2;</weak_warning>

    /* this suggested to check if protected can be applied */
    <weak_warning descr="Likely needs to be protected (already defined in \ClassOverridesField1).">private $privateFrom1;</weak_warning>

    /* and this is not reported */
    public $weakened;               // access level weakened
    static protected $staticFrom1;  // static fields are not checked
    const CLAZZ = __CLASS__;        // constants are not checked
}


/* False-positives: a test class */
class ClassOverridesFieldTest extends ClassOverridesField2 {
    private   $privateFrom1;
    private   $privateFrom2;
    protected $protectedFrom1;
    protected $protectedFrom2;
}
