package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.codeInspection.InspectionToolProvider;

/*
===POOL===

if ... {
} if (...) { -> probably "else if" intended to be here
} else {
}

call_user_func($singleArgument) -> $singleArgument()
call_user_func_array($singleArgument, array()) -> $singleArgument()
call_user_func(array($process, $method)) -> $process->{$method}();
    code style:

StaticInvocationViaThisInspector:
    - static calls on any objects, not only this (may be quite heavy due to index lookup)
    - configurable, ensure that PS not yet doing this

---

Empty functions/methods:
    - stubs, design issues

Empty try/catch
    - bad code, like no scream

PHP 5 migration: reflection API usage (ReflectionClass):
        constant, is_a, method_exists, property_exists, is_subclass_of are from PHP 4 world
        and not dealing with traits, annotations and so on. Mark deprecated.

*/
public class PhpInspectionsEAProvider implements InspectionToolProvider {
    @Override
    public Class[] getInspectionClasses() {
        return new Class[]{};
    }
}
