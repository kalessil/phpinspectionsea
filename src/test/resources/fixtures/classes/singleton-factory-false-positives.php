<?php

/* false-positives: only regular classes needs to be inspected */
trait SFPVTrait {
    protected function __construct() {}
}
interface SFPVInterface {
    function __construct();
}


/* false-positives: singletons with private/protected constructor */
class SingletonWithPrivateConstructor {
    private function __construct()       {}
    public static function getInstance() {}
}
class SingletonWithProtectedConstructor {
    protected function __construct()     {}
    public static function getInstance() {}
}