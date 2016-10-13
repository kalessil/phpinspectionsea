<?php

class classUpd
{
    private $p1;
    private $p2;
    private $p3;

    public function __construct($p1, $p2, $p3)
    {
        $this->p1 = $p1;
        $this->p2 = $p2;
        $this->p3 = $p3;
    }

    public function m1()
    {
        return [$this->p1];
    }

    public function m2()
    {
        return [$this->p2];
    }

    public function m3()
    {
        return [$this->p1, $this->p2];
    }
}