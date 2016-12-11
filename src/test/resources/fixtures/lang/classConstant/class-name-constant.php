<?php

namespace PhpInspections\EAExtended;

use PhpInspections\EAExtended\Prerequisites as PrerequisitesAlias;
use PhpInspections\EAExtended\Prerequisites;
use \stdClass as stdClassAlias;

/** @depends("Prerequisites") */
class Action {
    public function patterns() {
        $dependencies = [
            <weak_warning descr="Perhaps this can be replaced with PrerequisitesAlias::class">'PrerequisitesAlias'</weak_warning>,
            <weak_warning descr="Perhaps this can be replaced with stdClassAlias::class">'stdClassAlias'</weak_warning>,

            // same NS
            <weak_warning descr="Perhaps this can be replaced with Prerequisites::class">'Prerequisites'</weak_warning>,

            <weak_warning descr="Perhaps this can be replaced with \stdClass::class">'\stdClass'</weak_warning>,
            <weak_warning descr="Perhaps this can be replaced with \PhpInspections\EAExtended\Prerequisites::class">'\PhpInspections\EAExtended\Prerequisites'</weak_warning>,
            <weak_warning descr="Perhaps this can be replaced with \PhpInspections\EAExtended\Prerequisites::class">"\\PhpInspections\\EAExtended\\Prerequisites"</weak_warning>
        ];
    }

    public function falsePositives() {
        $x  = '';
        $x .= '\stdClass';

        return [
            '\stdClass' . $x,
            "\\stdClass{$x}",
            $x === '\stdClass',
        ];
    }
}