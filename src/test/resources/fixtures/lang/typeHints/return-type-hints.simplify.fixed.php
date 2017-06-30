<?php

use MyNamespace\MyOutsideClass;

function shouldSkipGlobalTypes(): \stdClass
{ return new \stdClass; }

function shouldSimplify(): MyOutsideClass
{ return new MyOutsideClass; }

function shouldNotDuplicateImport(): MyOutsideClass
{ return new MyOutsideClass; }
