<?php

trait AnEmptyTrait {
}

interface AnEmptyInterface {
}

class AnEmptyClassWithTraits {
    use AnEmptyTrait;
}

class AnEmptyException {
}

/**
 * @deprecated
 */
class AnEmptyDeprecatedClass {
}

class AClassWithAProperty {
    public $property;
}

class AClassWithAMethod {
    public function __construct()
    {
    }
}