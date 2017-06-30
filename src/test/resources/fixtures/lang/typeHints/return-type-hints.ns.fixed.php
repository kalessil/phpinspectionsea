<?php

namespace ParentNamespace {
    abstract class ClassFromParentNamespace {
        /** @return \ParentNamespace\ChildNamespace\InterfaceFromChildNamespace */
        public abstract function get(): ChildNamespace\InterfaceFromChildNamespace;
    }
}

namespace ParentNamespace\ChildNamespace {
    interface InterfaceFromChildNamespace {}
}
