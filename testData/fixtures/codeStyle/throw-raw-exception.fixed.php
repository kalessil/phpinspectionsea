<?php

    throw new \RuntimeException('...');
    throw new \RuntimeException('...');

    throw new RuntimeException();

    throw new RuntimeException('...');

    /* false-positive: custom constructor */
    class CustomizedConstructorException extends \RuntimeException {
        public function __construct() {}
    }
    throw new CustomizedConstructorException();

    /* false-positive: message override */
    class CustomizedMessageException extends \RuntimeException {
        protected $message = '...';
    }
    throw new CustomizedMessageException();