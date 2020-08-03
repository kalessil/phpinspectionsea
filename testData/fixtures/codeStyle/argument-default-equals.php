<?php

function functionWithDefaults($first = 'default', $second = 'default') {}

abstract class DefaultArgumentsAbstract {
    public function method($first = 'default', $second = 'default') {}
}
class DefaultArgumentsImplementation extends DefaultArgumentsAbstract {}

/* case: function reference */
functionWithDefaults(<weak_warning descr="[EA] The argument can be safely dropped, as it's identical to the default value.">'default'</weak_warning>);
functionWithDefaults('whatever', <weak_warning descr="[EA] The argument can be safely dropped, as it's identical to the default value.">'default'</weak_warning>);
functionWithDefaults(<weak_warning descr="[EA] The argument can be safely dropped, as it's identical to the default value.">'default', 'default'</weak_warning>);

/* case: proper elements deletion at QF */
functionWithDefaults(<weak_warning descr="[EA] The argument can be safely dropped, as it's identical to the default value.">'default', /* comment */ 'default'</weak_warning>);

/* false-positives: function reference */
functionWithDefaults();
functionWithDefaults('whatever');
functionWithDefaults('default', 'whatever');

$object = new DefaultArgumentsImplementation();

/* case: methods reference */
$object->method(<weak_warning descr="[EA] The argument can be safely dropped, as it's identical to the default value.">'default'</weak_warning>);
$object->method('whatever', <weak_warning descr="[EA] The argument can be safely dropped, as it's identical to the default value.">'default'</weak_warning>);
$object->method(<weak_warning descr="[EA] The argument can be safely dropped, as it's identical to the default value.">'default', 'default'</weak_warning>);

/* false-positives: method reference */
$object->method();
$object->method('whatever');
$object->method('default', 'whatever');

/* false-positives: type-safe array search */
in_array($x, $y, true);
in_array($x, $y, false);
array_search($x, $y, true);
array_search($x, $y, false);

/* false-positives: magic around dynamic variables manipulation */
functionWithDefaults('default', 'whatever', 'undeclared');
$object->method('default', 'whatever', 'undeclared');

/* false-positives: magic constants as defaults */
function functionWithMagicDefault($first = __LINE__) {}
functionWithMagicDefault(__LINE__);

function withDynamicArguments(string $parameter = '...') { return func_get_args(); }
withDynamicArguments('...');