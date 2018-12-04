<?php

class Aggregator
{
    use TraitOne;

    use <error descr="Provokes a fatal error ('one' method collision, see '\TraitOne').">TraitTwo</error>;
    use <error descr="Provokes a fatal error ('two' method collision, see '\TraitTwo').">TraitThree</error>;

    use TraitFour;
    public function three() {}
}

trait TraitOne
{
    public function one() {}
}

trait TraitTwo
{
    public function one() {}
    public function two() {}
}

trait TraitThree
{
    public function two() {}
    public function three() {}
}

trait TraitFour
{
    public function three() {}
}