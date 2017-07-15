<?php

    throw new <weak_warning descr="\Exception is too general. Consider throwing one of SPL exceptions instead.">Exception</weak_warning>('...');
    throw new <weak_warning descr="\Exception is too general. Consider throwing one of SPL exceptions instead.">\Exception</weak_warning>('...');

    throw <weak_warning descr="This exception is thrown without any message, please do yourself a favor and add it.">new RuntimeException()</weak_warning>;

    throw new RuntimeException('...');