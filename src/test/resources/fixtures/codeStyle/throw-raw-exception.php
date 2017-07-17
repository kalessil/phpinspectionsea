<?php

    throw new <weak_warning descr="\Exception is too general. Consider throwing one of SPL exceptions instead.">Exception</weak_warning>('...');
    throw new <weak_warning descr="\Exception is too general. Consider throwing one of SPL exceptions instead.">\Exception</weak_warning>('...');

    throw <weak_warning descr="This exception is thrown without a message. Consider adding one to help clarify or troubleshoot the exception.">new RuntimeException()</weak_warning>;

    throw new RuntimeException('...');

    class CustomizedException extends \RuntimeException {
        public function __construct() {}
    }
    throw new CustomizedException();