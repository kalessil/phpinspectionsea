<?php

    /* all operators and both functions */
    if (<weak_warning descr="'0 === strpos('first', 'second')' can be used instead (improves maintainability).">strncmp('first', 'second', 5) === 0</weak_warning>);
    if (<weak_warning descr="'0 !== strpos('first', 'second')' can be used instead (improves maintainability).">strncmp('first', 'second', 5) !== 0</weak_warning>);
    if (<weak_warning descr="'0 === strpos('first', 'second')' can be used instead (improves maintainability).">strncmp('first', 'second', 5) == 0</weak_warning>);
    if (<weak_warning descr="'0 !== strpos('first', 'second')' can be used instead (improves maintainability).">strncmp('first', 'second', 5) != 0</weak_warning>);
    if (<weak_warning descr="'0 === stripos('first', 'second')' can be used instead (improves maintainability).">strncasecmp('first', 'second', 5) === 0</weak_warning>);
    if (<weak_warning descr="'0 !== stripos('first', 'second')' can be used instead (improves maintainability).">strncasecmp('first', 'second', 5) !== 0</weak_warning>);

    /* yoda style */
    if (<weak_warning descr="'0 !== stripos('first', 'second')' can be used instead (improves maintainability).">0 !== strncasecmp('first', 'second', 5)</weak_warning>);

    /* context of booleans */
    if (<weak_warning descr="'0 !== strpos('first', 'second')' can be used instead (improves maintainability).">strncmp</weak_warning> ('first', 'second', 5));
    if (! <weak_warning descr="'0 === strpos('first', 'second')' can be used instead (improves maintainability).">strncmp</weak_warning> ('first', 'second', 5));
    if (! <weak_warning descr="'0 === stripos('first', 'second')' can be used instead (improves maintainability).">strncasecmp</weak_warning> ('first', 'second', 5));
