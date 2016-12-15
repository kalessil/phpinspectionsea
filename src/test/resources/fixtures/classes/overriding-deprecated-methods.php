<?php

    /** @deprecated */
    interface DeprecatedInterface {
        /** @deprecated */
        public function deprecatedInInterface();
    }
    /** @deprecated */
    class DeprecatedClass implements DeprecatedInterface {
        public function deprecatedInInterface() {} // <- reported
    }

    class DeprecatedMethod extends DeprecatedClass {
        /** @deprecated */
        public function deprecatedInClass(){}
    }

    class MyClass extends DeprecatedMethod {
        public function <warning descr="deprecatedInClass overrides/implements a deprecated method. Consider refactoring or deprecate it as well.">deprecatedInClass</warning> () {}
    }


    /* false-positives */
    class DeprecatedClassFixed implements DeprecatedInterface {
        /** @deprecated */
        public function deprecatedInInterface() {}
    }
    class MyClassFixed extends DeprecatedMethod {
        /** @deprecated */
        public function deprecatedInClass(){}
    }