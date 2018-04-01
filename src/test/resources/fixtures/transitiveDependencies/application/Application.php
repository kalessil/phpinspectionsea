<?php

return [
    new \stdClass(),
    new \First\Clazz(),
    new <warning descr="A transitive dependency has been introduced, please actualize composer manifest dependencies.">\Second\Clazz()</warning>,
];