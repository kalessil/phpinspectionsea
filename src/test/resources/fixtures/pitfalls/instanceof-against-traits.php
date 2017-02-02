<?php

trait IOAT_Trait {}

$x = <error descr="instanceof against traits returns 'false'.">$z instanceof IOAT_Trait</error>;
$x = <error descr="instanceof against traits returns 'false'.">$z instanceof IOAT_Trait::class</error>;
$x = <error descr="instanceof against traits returns 'false'.">$z instanceof \IOAT_Trait</error>;