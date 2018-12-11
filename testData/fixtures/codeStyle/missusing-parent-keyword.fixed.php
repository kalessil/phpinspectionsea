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
        $this->overridden('...', '...');
        parent::blocked();
    }
}
class BlockerClass extends CasesHolder
{
    public function blocked() {}
}
