<?php

namespace PhpInspections\EAExtended;

use PhpInspections\EAExtended\Prerequisites as PrerequisitesAlias;
use PhpInspections\EAExtended\Prerequisites;
use \stdClass as stdClassAlias;
use PhpInspections\Base\Benefits;

trait LocalTrait {}

/** @depends("Prerequisites") */
class Action {
    use LocalTrait;

    public function patterns() {
        $isExists = class_exists(stdClassAlias::class);

        $dependencies = [
            'PrerequisitesAlias',
            'stdClassAlias',
            'Prerequisites',

            static::class,
            parent::class,

            stdClassAlias::class,
            stdClassAlias::class,
            Prerequisites::class,
            Prerequisites::class,
            Prerequisites::class,
            Prerequisites::class,
            Prerequisites::class,
            Prerequisites::class,

            Sub\Benefits::class,

            \PhpInspections\Base\Prerequisites::class,
            Benefits::class
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