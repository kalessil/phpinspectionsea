<?php

trait PropertiesTraitForBase1 {
    public $baseSame          = true;
    private $baseDifferent1   = false;
}
trait PropertiesTraitForBase2 {
    protected $baseDifferent2 = false;
}

/**
 * @property $privatePhpdocProperty
 * @property $protectedPhpdocProperty
 * @property $publicPhpdocProperty
 */
trait PropertiesTrait {
    /** @Id */
    public $sameAnnotated     = true;
    public $same              = true;
    private $different        = false;
}

class BasePropertiesExample {
    public $baseSame          = true;
    private $baseDifferent1   = true;

    protected $baseDifferent2 = true;
}
class PropertiesExample extends BasePropertiesExample {
    use
        <weak_warning descr="'PropertiesExample' and 'PropertiesTraitForBase1' define the same property ($baseSame).">PropertiesTraitForBase1</weak_warning>,
        <error descr="'PropertiesExample' and 'PropertiesTraitForBase2' define the same property ($baseDifferent2).">PropertiesTraitForBase2</error>
    ;

    use PropertiesTrait;
        /** @Id */
    public $sameAnnotated = true;
    public <weak_warning descr="'PropertiesExample' and 'PropertiesTrait' define the same property ($same).">$same</weak_warning> = true;
    private $different = true;
    
    private $privatePhpdocProperty = '...';
    protected $protectedPhpdocProperty = '...';
    public $publicPhpdocProperty = '...';
}