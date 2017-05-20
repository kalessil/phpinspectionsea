<?php

function functionWithDefaults($first = 'default', $second = 'default') {}

abstract class DefaultArgumentsAbstract {
    public function method($first = 'default', $second = 'default') {}
}
class DefaultArgumentsImplementation extends DefaultArgumentsAbstract {}

/* case: function reference */
functionWithDefaults();
functionWithDefaults('whatever');
functionWithDefaults();

/* case: proper elements deletion at QF */
functionWithDefaults();

/* false-positives: function reference */
functionWithDefaults();
functionWithDefaults('whatever');
functionWithDefaults('default', 'whatever');

$object = new DefaultArgumentsImplementation();

/* case: methods reference */
$object->method();
$object->method('whatever');
$object->method();

/* false-positives: method reference */
$object->method();
$object->method('whatever');
$object->method('default', 'whatever');