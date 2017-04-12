<?php

namespace ParentNamespace {
    abstract class ClassFromParentNamespace {
        /** @return \ParentNamespace\ChildNamespace\InterfaceFromChildNamespace */
        public abstract function <weak_warning descr="': ChildNamespace\InterfaceFromChildNamespace' can be declared as return type hint.">get</weak_warning>();
    }
}

namespace ParentNamespace\ChildNamespace {
    interface InterfaceFromChildNamespace {}
}