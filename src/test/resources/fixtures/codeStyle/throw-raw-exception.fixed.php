<?php

    throw new RuntimeException('...');
    throw new \RuntimeException('...');

    throw new RuntimeException();

    throw new RuntimeException('...');

    class CustomizedException extends \RuntimeException {
        public function __construct() {}
    }
    throw new CustomizedException();