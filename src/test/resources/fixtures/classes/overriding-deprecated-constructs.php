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
        public function deprecatedInClass(){}
    }
    class MyClass extends DeprecatedMethod {
        public function <warning descr="'deprecatedInClass' overrides/implements a deprecated method. Consider refactoring or deprecate it as well.">deprecatedInClass</warning> () {}
    }
    interface MyInterface extends DeprecatedInterface {
        public function <warning descr="'deprecatedInInterface' overrides/implements a deprecated method. Consider refactoring or deprecate it as well.">deprecatedInInterface</warning>();
    }


    /* false-positives: implemented deprecation and deprecated */
    class DeprecatedClassFixed implements DeprecatedInterface {
        /** @deprecated */
        public function deprecatedInInterface() {}
    }
    /* false-positives: overrides a deprecation and deprecated */
    class MyClassFixed extends DeprecatedMethod {
        /** @deprecated */
        public function deprecatedInClass(){}
    }