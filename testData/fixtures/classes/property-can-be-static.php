<?php

class ParentClass {}

class CasesHolder extends ParentClass {
    private <weak_warning descr="This property initialization seems to be quite 'heavy', consider using static property instead.">$reportedPrivate3Strings</weak_warning>           = ['', '', ''];
    private <weak_warning descr="This property initialization seems to be quite 'heavy', consider using static property instead.">$reportedPrivate3Arrays</weak_warning>            = [[], [], []];
    private <weak_warning descr="This property initialization seems to be quite 'heavy', consider using static property instead.">$reportedPrivate3ArraysIndexed</weak_warning>     = [0 => [], 1 => [], 2 => []];
    protected <weak_warning descr="This property initialization seems to be quite 'heavy', consider using static property instead.">$reportedProtected3Strings</weak_warning>       = ['', '', ''];
    protected <weak_warning descr="This property initialization seems to be quite 'heavy', consider using static property instead.">$reportedProtected3Arrays</weak_warning>        = [[], [], []];
    protected <weak_warning descr="This property initialization seems to be quite 'heavy', consider using static property instead.">$reportedProtected3ArraysIndexed</weak_warning> = [0 => [], 1 => [], 2 => []];

    private $private2Strings     = ['', ''];
    protected $protected2Strings = ['', ''];
    public $public               = ['', '', ''];
    static private $static       = ['', '', ''];
}