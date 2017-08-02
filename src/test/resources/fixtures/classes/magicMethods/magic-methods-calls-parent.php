<?php

/* case: missing parent call */
class MissingParent {
    public function __construct() {}
}
class MissingImplementation extends MissingParent {
    private function <error descr="__construct is probably missing MissingParent::__construct call.">__construct</error>() {}
}

/* false-positives: parent method is abstract */
abstract class AbstractParent {
    public abstract function __construct();
}
class AbstractImplementation {
    public function __construct() {}
}

/* false-positives: parent method is private */
class PrivateParent {
    private function __construct() {}
}
class PrivateImplementation extends PrivateParent {
    private function __construct() {}
}

/* false-positive: presented parent call */
class PresentedParent {
    public function __construct() {}
}
class PresentedImplementation extends PresentedParent {
    private function __construct() {
        parent::__construct();
    }
}
