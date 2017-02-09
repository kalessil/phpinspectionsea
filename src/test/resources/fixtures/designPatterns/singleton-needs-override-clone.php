<?php

class <weak_warning descr="Singleton should also override __clone to prevent copying the instance">EntityNeedsOverrideClone</weak_warning>
{
    private function __construct()       {}
    public static function getInstance() {}
}