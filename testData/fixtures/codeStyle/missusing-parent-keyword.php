<?php

class ParentClass
{
    public function overridden() {}
    public function blocked()    {}
}

class ChildClass extends ParentClass
{
    public function overridden()
    {
        parent::overridden();
    }
}

class CasesHolder extends ParentClass
{
    public function target()
    {
        <weak_warning descr="It was probably intended to use '$this' here.">parent</weak_warning>::overridden();
        parent::blocked();
    }
}
class BlockerClass extends CasesHolder
{
    public function blocked() {}
}
