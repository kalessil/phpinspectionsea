<?php

    throw new <weak_warning descr="\Exception is too general. Consider throwing one of SPL exceptions instead.">Exception</weak_warning>('...');
    throw new <weak_warning descr="\Exception is too general. Consider throwing one of SPL exceptions instead.">\Exception</weak_warning>('...');

    throw <weak_warning descr="This exception is thrown without a message. Consider adding one to help clarify or troubleshoot the exception.">new RuntimeException()</weak_warning>;

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