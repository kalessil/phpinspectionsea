package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.NamingConvention;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class NamingRules {

    final private HashMap<String, Set<NamingRule>> configuration = new HashMap<>();

    public void add(NamingRule rule) {
        final String type = rule.getType();
        Set<NamingRule> map = getRulesByType(type);
        map.add(rule);
        configuration.put(type, map);
    }

    Set<NamingRule> getRulesByType(String type) {
        Set<NamingRule> map = configuration.get(type);
        if (map == null) {
            map = new HashSet<>();
        }
        return map;
    }

}
