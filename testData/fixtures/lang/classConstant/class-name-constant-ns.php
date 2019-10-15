<?php

namespace PhpInspections\EAExtended;

use PhpInspections\EAExtended\Prerequisites as PrerequisitesAlias;
use PhpInspections\EAExtended\Prerequisites;
use \stdClass as stdClassAlias;

trait LocalTrait {}

/** @depends("Prerequisites") */
class Action {
    use LocalTrait;

    public function patterns() {
        $isExists = class_exists(<weak_warning descr="[EA] Perhaps this can be replaced with \stdClass::class.">'\stdClass'</weak_warning>);

        $dependencies = [
            'PrerequisitesAlias',
            'stdClassAlias',
            'Prerequisites',

            <weak_warning descr="[EA] 'static::class' can be used instead.">get_called_class()</weak_warning>,

            <weak_warning descr="[EA] Perhaps this can be replaced with \stdClass::class.">'\stdClass'</weak_warning>,
            <weak_warning descr="[EA] Perhaps this can be replaced with \stdClass::class.">'stdClass'</weak_warning>,
            <weak_warning descr="[EA] Perhaps this can be replaced with \PhpInspections\EAExtended\Prerequisites::class.">'\PhpInspections\EAExtended\Prerequisites'</weak_warning>,
            <weak_warning descr="[EA] Perhaps this can be replaced with \PhpInspections\EAExtended\Prerequisites::class.">'PhpInspections\EAExtended\Prerequisites'</weak_warning>,
            <weak_warning descr="[EA] Perhaps this can be replaced with \PhpInspections\EAExtended\Prerequisites::class.">"\\PhpInspections\\EAExtended\\Prerequisites"</weak_warning>,
            <weak_warning descr="[EA] Perhaps this can be replaced with \PhpInspections\EAExtended\Prerequisites::class.">"PhpInspections\\EAExtended\\Prerequisites"</weak_warning>,

            <weak_warning descr="[EA] Perhaps this can be replaced with \PhpInspections\EAExtended\Sub\Benefits::class.">'PhpInspections\EAExtended\Sub\Benefits'</weak_warning>,

            <weak_warning descr="[EA] Perhaps this can be replaced with \PhpInspections\Base\Prerequisites::class.">'PhpInspections\Base\Prerequisites'</weak_warning>,
            <weak_warning descr="[EA] Perhaps this can be replaced with \PhpInspections\Base\Benefits::class.">'PhpInspections\Base\Benefits'</weak_warning>
        ];
    }

    public function falsePositives() {
        $x  = '';
        $x .= '\stdClass';

        return [
            'stdclass',
            'stdclassalias',
            '\stdClass' . $x,
            "\\stdClass{$x}",
            $x === '\stdClass',
        ];
    }
}