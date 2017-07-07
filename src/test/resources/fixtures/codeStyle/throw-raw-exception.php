<?php

    throw new <weak_warning descr="\Exception is too general. Consider throwing one of SPL exceptions instead.">Exception</weak_warning>('...');
    throw new <weak_warning descr="\Exception is too general. Consider throwing one of SPL exceptions instead.">\Exception</weak_warning>('...');

    throw <weak_warning descr="The exception is throw without any message, please make a favor to yourself and add it.">new RuntimeException()</weak_warning>;

    throw new RuntimeException('...');