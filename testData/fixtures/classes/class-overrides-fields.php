<?php

trait TraitWithPrivateField {
    private $privateFromTrait;
}
class ClassOverridesField1 {
    use TraitWithPrivateField;

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


/** @property $privateFrom2 */
class ClassOverridesField extends ClassOverridesField2 {
    protected <weak_warning descr="[EA] Field 'protectedFrom1' is already defined in \ClassOverridesField1, check our online documentation for options.">$protectedFrom1</weak_warning>;
    protected <weak_warning descr="[EA] Field 'protectedFrom2' is already defined in \ClassOverridesField2, check our online documentation for options.">$protectedFrom2</weak_warning>;

    private <weak_warning descr="[EA] Likely needs to be renamed in sake of maintainability (private property with the same name already defined in \ClassOverridesField1).">$privateFrom1</weak_warning>;

    private <weak_warning descr="[EA] Likely needs to be renamed in sake of maintainability (private property with the same name already defined in \TraitWithPrivateField).">$privateFromTrait</weak_warning>;

    public $weakened;               // access level relaxed
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

/* False-positive: doctrine entities*/
class ParentEntity {
    protected $id;
}
class ChildEntity extends ParentEntity {
    /** @ORM\Column("...") */
    protected $id;
}

/* False-positive: final constructor */
class ParentEntityWithFinalConstructor {
    protected $id;
    final public function __construct() {}
}
class ChildOfParentEntityWithFinalConstructor extends ParentEntityWithFinalConstructor {
    protected $id;
}

/* False-positive: private constructor */
class ParentEntityWithPrivateConstructor {
    protected $id;
    private function __construct() {}
}
class ChildOfParentEntityWithPrivateConstructor extends ParentEntityWithPrivateConstructor {
    protected $id;
}