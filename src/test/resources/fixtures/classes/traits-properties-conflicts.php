<?php

trait PropertiesTraitForBase1 {
    public $baseSame          = true;
    private $baseDifferent1   = false;
}
trait PropertiesTraitForBase2 {
    protected $baseDifferent2 = false;
}
trait PropertiesTrait {
    public $same              = true;
    private $different        = false;
}

class BasePropertiesExample {
    public $baseSame          = true;
    private $baseDifferent1   = true;

    protected $baseDifferent2 = true;
}
class PropertiesExample extends BasePropertiesExample {
    use PropertiesTraitForBase1; // Warning: $baseSame; Strict Standards => report in the use
    use PropertiesTraitForBase2; // Error: $baseDifferent2; Fatal error => report in the use

    use PropertiesTrait;
    public <weak_warning descr="'PropertiesExample' and 'PropertiesTrait' define the same property ($same).">$same</weak_warning>
        = true;
    private <error descr="PropertiesExample and PropertiesTrait define the same property different">$different</error>
        = true;
}