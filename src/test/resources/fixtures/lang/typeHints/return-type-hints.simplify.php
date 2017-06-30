<?php

function <weak_warning descr="\stdClass can be declared as return type hint">shouldSkipGlobalTypes</weak_warning>()
{ return new \stdClass; }

function <weak_warning descr="\MyNamespace\MyOutsideClass can be declared as return type hint">shouldSimplify</weak_warning>()
{ return new \MyNamespace\MyOutsideClass; }

function <weak_warning descr="\MyNamespace\MyOutsideClass can be declared as return type hint">shouldNotDuplicateImport</weak_warning>()
{ return new \MyNamespace\MyOutsideClass; }
