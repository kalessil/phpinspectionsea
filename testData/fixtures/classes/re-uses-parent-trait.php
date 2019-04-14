<?php

namespace Traits {
    trait TargetTrait {}
    trait ProxyTrait  { use TargetTrait; }
}

namespace Classes {
    use Traits\ProxyTrait, Traits\TargetTrait, Traits\TargetTrait as TargetTraitAlias;

    class <warning descr="'\Traits\TargetTrait' is already used.">ClassWithDirectlyDuplicateTraits</warning> {
        use TargetTrait, TargetTraitAlias;
    }
    class <warning descr="'\Traits\TargetTrait' is already used in '\Traits\ProxyTrait'.">ClassWithDuplicateTraitsViaProxyTraits</warning> {
        use TargetTrait, ProxyTrait;
    }

    class ParentClassWithProxyTrait { use ProxyTrait; }
    class <warning descr="'\Traits\TargetTrait' is already used in '\Traits\ProxyTrait'.">ClassWithDuplicateTraitsViaParentClass</warning> extends ParentClassWithProxyTrait {
        use TargetTraitAlias;
    }
}