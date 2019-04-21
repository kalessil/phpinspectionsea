<?php

    /* pattern: implementing deprecation */
    interface DeprecatedInterface {
        /** @deprecated */
        public function deprecatedInInterface();
    }
    class DeprecatedClass implements DeprecatedInterface {
        public function <warning descr="'deprecatedInInterface' overrides/implements a deprecated method. Consider refactoring or deprecate it as well.">deprecatedInInterface</warning> () {}
    }

    /* pattern: overriding deprecation */
    class DeprecatedMethod extends DeprecatedClass {
        /** @deprecated */
        public function deprecatedInClassFirst(){}
        /** @deprecated */
        public function deprecatedInClassSecond(){}
    }
    class MyClass extends DeprecatedMethod {
        public function <warning descr="'deprecatedInClassFirst' overrides/implements a deprecated method. Consider refactoring or deprecate it as well.">deprecatedInClassFirst</warning> () {}
        /** @deprecated */
        public function deprecatedInClassSecond(){}
    }
    interface MyInterface extends DeprecatedInterface {
        public function <warning descr="'deprecatedInInterface' overrides/implements a deprecated method. Consider refactoring or deprecate it as well.">deprecatedInInterface</warning>();
    }

    /* pattern: overriding trait deprecation */
    trait TraitWithDeprecations {
        /** @deprecated */
        public function deprecatedInTrait(){}
    }
    class ClassWithTrait {
        use TraitWithDeprecations;
        public function <warning descr="'deprecatedInTrait' overrides/implements a deprecated method. Consider refactoring or deprecate it as well.">deprecatedInTrait</warning>(){}
    }

    /* pattern: child deprecation instead of parent */
    abstract class DeprecationHolderParent {
        abstract public function abstractToDeprecate();
        public function implementationToDeprecate() {}
    }
    abstract class DeprecationHolderChild extends DeprecationHolderParent {
        /** @deprecated */
        public function <warning descr="The overridden/implemented 'abstractToDeprecate' probably needs to be deprecated as well.">abstractToDeprecate</warning> () {}
        /** @deprecated */
        public function <warning descr="The overridden/implemented 'implementationToDeprecate' probably needs to be deprecated as well.">implementationToDeprecate</warning> () {}
    }

    /* false-positives: implemented deprecation and deprecated */
    class DeprecatedClassFixed implements DeprecatedInterface {
        /** @deprecated */
        public function deprecatedInInterface() {}
    }
    /* false-positives: overrides a deprecation and deprecated */
    class MyClassFixed extends DeprecatedMethod {
        /** @deprecated */
        public function deprecatedInClassFirst(){}
    }

    class MissingDeprecationTags {
        public function <warning descr="'regular' triggers a deprecation warning, but misses @deprecated annotation.">regular</warning>()  { trigger_error('...', E_USER_DEPRECATED); }
        public function <warning descr="'suppressed' triggers a deprecation warning, but misses @deprecated annotation.">suppressed</warning>() { @trigger_error('...', E_USER_DEPRECATED); }

        /** @deprecated  */
        public function deprecated()  { trigger_error('...', E_USER_DEPRECATED); }
        public function conditional() { if(true) { trigger_error('...', E_USER_DEPRECATED); } }
    }