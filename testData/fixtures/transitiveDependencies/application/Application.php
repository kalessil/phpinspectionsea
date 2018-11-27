<?php

namespace Company {
    use First\Subsplit\Clazz as SubsplitClazz;
    use First\Clazz as FirstClass;
    use <warning descr="The class belongs to a package which is not directly required in your composer.json. Please add the package into your composer.json">Second\Clazz</warning> as SecondClass;
    use Ignored\Clazz as IgnoredClass;

    class Application extends SecondClass {
        public function method() {
            return [
                new self(),
                new static(),

                new \stdClass(),
                new \First\Subsplit\Clazz(),
                new \First\Clazz(),
                new <warning descr="The class belongs to a package which is not directly required in your composer.json. Please add the package into your composer.json">\Second\Clazz</warning>(),
                new \Ignored\Clazz(),

                new SubsplitClazz(),
                new FirstClass(),
                new SecondClass(),
                new IgnoredClass(),
            ];
        }
    }
}

namespace Company {
    /* the ignored one is by ignore-configuration */
    return [
        new \stdClass(),
        new \First\Clazz(),
        new <warning descr="The class belongs to a package which is not directly required in your composer.json. Please add the package into your composer.json">\Second\Clazz</warning>(),
        new \Ignored\Clazz(),
    ];
}