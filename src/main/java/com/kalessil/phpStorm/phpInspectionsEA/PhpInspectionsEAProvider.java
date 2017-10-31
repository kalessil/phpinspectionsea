package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.codeInspection.InspectionToolProvider;

/*
Console reports some errors here:
at com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromSignatureResolvingUtil.
resolveSignature(TypeFromSignatureResolvingUtil.java:177)


===POOL===

if ... {
} if (...) { -> probably "else if" intended to be here
} else {
}

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
