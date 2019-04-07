<?php

    /* pattern: implementing deprecation */
    class ClassDeprecatesMethod {
        /** @deprecated */
        public function deprecatedInClass(){}
    }
    class ClassMissesDeprecation extends ClassDeprecatesMethod {
        public function <warning descr="'deprecatedInClass' overrides/implements a deprecated method. Consider refactoring or deprecate it as well.">deprecatedInClass</warning> () {}
    }
    class ClassDeprecatesProperly extends ClassDeprecatesMethod {
        /** @deprecated */
        public function deprecatedInClass(){}
    }

    /* pattern: child deprecation instead of parent */
    abstract class DeprecationHolderParent {
        abstract public function abstractToDeprecate();
        public function implementationToDeprecate() {}
    }
    abstract class DeprecationHolderChild extends DeprecationHolderParent {
        /** @deprecated */
        public function <weak_warning descr="Parent 'abstractToDeprecate' probably needs to be deprecated as well.">abstractToDeprecate</weak_warning> () {}
        /** @deprecated */
        public function <weak_warning descr="Parent 'implementationToDeprecate' probably needs to be deprecated as well.">implementationToDeprecate</weak_warning> () {}
    }
