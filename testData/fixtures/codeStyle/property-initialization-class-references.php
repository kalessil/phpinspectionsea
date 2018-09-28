<?php

namespace NamespaceOne {
    class Collision {}

    class ParentClass {
        protected $property = Collision::class;
    }
}

namespace NamespaceTwo {
    class Collision {}

    class ChildClass extends \NamespaceOne\ParentClass {
        protected $property = Collision::class;
    }
}