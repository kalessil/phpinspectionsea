<?php


class BasePCBS {
    private $private     = [];
    protected $protected = [];
}

class MiddlewarePCBS extends BasePCBS {
}

class ImplementationPCBS extends MiddlewarePCBS {
    private $private       = ['', '', ''];
    protected $protected   = ['', '', ''];
    public $public         = ['', '', ''];
    static private $static = ['', '', ''];
}