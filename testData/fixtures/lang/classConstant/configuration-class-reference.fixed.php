<?php

declare(strict_types=1);

use PhpInspections\EAExtended\Clazz;

return [
    Clazz::class,
    Clazz::class,
    class_alias('...', '\PhpInspections\EAExtended\Clazz'),
];