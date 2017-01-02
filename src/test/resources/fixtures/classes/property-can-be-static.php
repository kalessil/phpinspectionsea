<?php

class BasePCBS {
}

class ChildPCBS extends BasePCBS {
    private <weak_warning descr="This property initialization seems to be quite 'heavy', probably it should be defined as static.">$reportedPrivate3Strings</weak_warning>            = ['', '', ''];
    private <weak_warning descr="This property initialization seems to be quite 'heavy', probably it should be defined as static.">$reportedPrivate3Arrays</weak_warning>             = [[], [], []];
    private <weak_warning descr="This property initialization seems to be quite 'heavy', probably it should be defined as static.">$reportedPrivate3ArraysIndexed</weak_warning>      = [0 => [], 1 => [], 2 => []];
    protected <weak_warning descr="This property initialization seems to be quite 'heavy', probably it should be defined as static.">$reportedProtected3Strings</weak_warning>        = ['', '', ''];
    protected <weak_warning descr="This property initialization seems to be quite 'heavy', probably it should be defined as static.">$reportedProtected3Arrays</weak_warning>         = [[], [], []];
    protected <weak_warning descr="This property initialization seems to be quite 'heavy', probably it should be defined as static.">$reportedProtected3ArraysIndexed</weak_warning>  = [0 => [], 1 => [], 2 => []];

    private $private2Strings     = ['', ''];
    protected $protected2Strings = ['', ''];
    public $public               = ['', '', ''];
    static private $static       = ['', '', ''];
}