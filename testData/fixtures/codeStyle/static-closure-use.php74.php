<?php

$unscopedFactories = [
    CasesHolder::class => <weak_warning descr="[EA] This closure can be declared as static (better scoping; in some cases can improve performance).">fn</weak_warning>() => null,
    CasesHolder::class => static fn() => null,
];