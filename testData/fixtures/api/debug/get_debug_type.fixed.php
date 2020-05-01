<?php

return [
    get_debug_type($variable),
    is_object($variable) ? get_class($variable) : gettype([]),
    is_object($variable) ? get_class($variable) : 'n/a',
];