<?php

return [
    <weak_warning descr="[EA] Can be replaced by 'get_debug_type($variable)' (improves maintainability).">is_object($variable) ? get_class($variable) : gettype($variable)</weak_warning>,
    is_object($variable) ? get_class($variable) : gettype([]),
    is_object($variable) ? get_class($variable) : 'n/a',
];