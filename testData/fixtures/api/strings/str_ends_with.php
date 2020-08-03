<?php

return [
    <weak_warning descr="[EA] Can be replaced by 'str_ends_with('haystack', 'needle')' (improves maintainability).">substr('haystack', -strlen('needle')) === 'needle'</weak_warning>,
    <weak_warning descr="[EA] Can be replaced by 'str_ends_with('haystack', 'needle')' (improves maintainability).">mb_substr('haystack', - mb_strlen('needle')) === 'needle'</weak_warning>,

    <weak_warning descr="[EA] Can be replaced by '! str_ends_with('haystack', 'needle')' (improves maintainability).">substr('haystack', -strlen('needle')) !== 'needle'</weak_warning>,
    <weak_warning descr="[EA] Can be replaced by '! str_ends_with('haystack', 'needle')' (improves maintainability).">mb_substr('haystack', - mb_strlen('needle')) !== 'needle'</weak_warning>,

    substr('haystack', -strlen('needle')) !== '...',
    substr('haystack', -strlen('needle'), 1) !== 'needle',
    mb_substr('haystack', - mb_strlen('needle', '...')) === 'needle',
];