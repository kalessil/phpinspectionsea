<?php

class ParentClass
{
    public function overriddenRegular()       {}
    static public function overriddenStatic() {}
    public function blocked()                 {}
}

class ChildClass extends ParentClass
{
    public function overriddenRegular()
    {
        parent::overriddenRegular();
    }
}

class CasesHolder extends ParentClass
{
    public function target()
    {
        $this->overriddenRegular('...', '...');
        self::overriddenStatic('...', '...');
        parent::blocked();
    }
}
class BlockerClass extends CasesHolder
{
    public function blocked() {}
}
