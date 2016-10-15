<?php

class sigletonClass // <- reported
{
    public function __construct() {}
    public function getInstance() {}
}

class factoryClass // <- reported
{
    protected function __construct() {}
}