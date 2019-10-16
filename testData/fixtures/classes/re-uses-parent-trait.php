<?php

namespace Traits {
    trait TargetTrait {}
    trait ProxyTrait  { use TargetTrait; }
}

namespace Classes {
    use Traits\ProxyTrait, Traits\TargetTrait, Traits\TargetTrait as TargetTraitAlias;

    class ClassWithDirectlyDuplicateTraits {
        use TargetTrait,
            <warning descr="[EA] '\Traits\TargetTrait' is already used in this same class.">TargetTraitAlias</warning>;
    }
    class ClassWithDuplicateTraitsViaProxyTraits {
        use <warning descr="[EA] '\Traits\TargetTrait' is already used in '\Traits\ProxyTrait'.">TargetTrait</warning>,
            ProxyTrait;
    }

    class ParentClassWithProxyTrait { use ProxyTrait; }
    class ClassWithDuplicateTraitsViaParentClass extends ParentClassWithProxyTrait {
        use <warning descr="[EA] '\Traits\TargetTrait' is already used in '\Traits\ProxyTrait'.">TargetTraitAlias</warning>;
    }
}