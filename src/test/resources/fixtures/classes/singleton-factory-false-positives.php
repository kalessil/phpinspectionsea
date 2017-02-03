<?php

/* false-positives: only regular classes needs to be inspected */
trait SFPVTrait {
    protected function __construct() {}
}
interface SFPVInterface {
    function __construct();
}
