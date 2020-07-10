<?php

declare(strict_types=1);

class LocalClass {}

return [
    <weak_warning descr="[EA] Perhaps this can be replaced with \PhpInspections\EAExtended\Clazz::class.">'PhpInspections\EAExtended\Clazz'</weak_warning>,
    <weak_warning descr="[EA] Perhaps this can be replaced with \PhpInspections\EAExtended\Clazz::class.">'\PhpInspections\EAExtended\Clazz'</weak_warning>,
    class_alias('...', '\LocalClass'),
];