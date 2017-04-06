package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.NamingConvention;


import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NamingRules {

    final private Map<NamingRule.ObjectType, Set<NamingRule>> configuration = new HashMap<>();

    public void add(@NotNull NamingRule rule) {
        final NamingRule.ObjectType type = rule.getType();
        Set<NamingRule> map = getRulesByType(type);
        map.add(rule);
        configuration.put(type, map);
    }

    Set<NamingRule> getRulesByType(@NotNull NamingRule.ObjectType type) {
        Set<NamingRule> map = configuration.get(type);
        if (map == null) {
            map = new HashSet<>();
        }
        return map;
    }

}
