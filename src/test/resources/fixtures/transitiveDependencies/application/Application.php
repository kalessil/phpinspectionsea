<?php

return [
    new \stdClass(),
    new \First\Clazz(),
    new <warning descr="A transitive dependency has been introduced, please add the dependency into composer manifest.">\Second\Clazz()</warning>,
];