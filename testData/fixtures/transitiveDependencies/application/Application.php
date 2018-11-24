<?php

/* the ignored one is by ignore-configuration */
return [
    new \stdClass(),
    new \First\Clazz(),
    new <warning descr="The class belongs to a package which is not directly required in your composer.json. Please add the package into your composer.json">\Second\Clazz()</warning>,
    new \Ignored\Clazz(),
];