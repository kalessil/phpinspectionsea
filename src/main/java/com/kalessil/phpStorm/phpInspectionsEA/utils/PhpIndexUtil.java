package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

final public class PhpIndexUtil {

    @NotNull
    static public List<PhpClass> getObjectInterfaces(@NotNull String name, @NotNull PhpIndex index) {
        final List<PhpClass> result = new ArrayList<>();
        result.addAll(OpenapiResolveUtil.resolveClassesByFQN(name, index));
        result.addAll(OpenapiResolveUtil.resolveInterfacesByFQN(name, index));
        return result;
    }
}
