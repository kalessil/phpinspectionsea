<?php

declare(strict_types=1);

use PhpInspections\EAExtended\Clazz;

class LocalClass {}

return [
    Clazz::class,
    Clazz::class,
    class_alias('...', '\LocalClass'),
];