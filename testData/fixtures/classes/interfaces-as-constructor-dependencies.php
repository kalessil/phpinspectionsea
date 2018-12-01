<?php

namespace {
    interface Legacy_Autoloaded_Interface {}
}

namespace One {
    interface Ps4AutoloadedInterface {}

    class FromLegacyApplication implements \Legacy_Autoloaded_Interface {}
    class FromModernApplication implements Ps4AutoloadedInterface       {}
    abstract class BaseComponent                                        {}

    class ProtoductionComponent {}

    class Clazz {
        /** @var ProtoductionComponent $typehinted */
        public function __construct(
            string $string,
            $typehinted,
            BaseComponent $abstract,
            <weak_warning descr="The parameters' type should be replaced with an interface (contract) (extensibility concerns).">?FromLegacyApplication $legacy</weak_warning>,
            <weak_warning descr="The parameters' type should be replaced with an interface (contract) (extensibility concerns).">?FromModernApplication $modern</weak_warning>,
            <weak_warning descr="The parameters' class doesn't implement any interfaces (contracts), consider introducing one (extensibility concerns).">?ProtoductionComponent $protoduction</weak_warning>
        ) {}
    }
}