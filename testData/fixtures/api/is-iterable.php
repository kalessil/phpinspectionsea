<?php

    $target = <weak_warning descr="[EA] 'is_array($variable) || $variable instanceof Traversable' can be replaced by 'is_iterable($variable)'.">is_array($variable)</weak_warning> || $variable || $variable instanceof \Traversable;