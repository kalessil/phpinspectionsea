package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

public class CompositionAndInheritanceInspector {
    /*
     * option: require all classes be final/abstract
     *
     * If class is not yet final/abstract/trait/interface
     * If class implements interfaces and doesn't extend other class
     * If all classes' public methods are due to interfaces
     *
     * If has child classes -> abstract
     * If doesn't have child classes -> final
     */
}
