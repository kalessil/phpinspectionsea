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
        <weak_warning descr="[EA] It was probably intended to use '$this->overriddenRegular('...', '...')' here.">parent::overriddenRegular('...', '...')</weak_warning>;
        <weak_warning descr="[EA] It was probably intended to use 'self::overriddenStatic('...', '...')' here.">parent::overriddenStatic('...', '...')</weak_warning>;
        parent::blocked();
    }
}
class BlockerClass extends CasesHolder
{
    public function blocked() {}
}
