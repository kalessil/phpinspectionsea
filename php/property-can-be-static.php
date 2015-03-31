<?php

class CheckForStaticPropertiesCandidate
{
    /** no warnings expected */
    protected $protectedStrings = array('One', 'Two', 'Three');
    protected $protectedArrays = array(array('One'), array('Two'), array('Three'));
    protected $protectedMixed = array('One', array('Two'), array('Three'));
    protected $protectedMixed2 = array('One', 1 => array('Two'), array('Three'));
    protected $protectedStringsIndexed = array(0 => 'One', 1 => 'Two', 2 => 'Three');
    protected $protectedArraysIndexed = array(0 => array('One'), 1 => array('Two'), 2 => array('Three'));

    /** warnings expected */
    private $privateStrings = array('One', 'Two', 'Three');
    private $privateArrays = array(array('One'), array('Two'), array('Three'));
    private $privateMixed = array('One', array('Two'), array('Three'));
    private $privateStringsIndexed = array(0 => 'One', 1 => 'Two', 2 => 'Three');
    private $privateArraysIndexed = array(0 => array('One'), 1 => array('Two'), 2 => array('Three'));

    /** no warnings expected */
    public $publicStrings = array('One', 'Two', 'Three');

    /** no warnings expected */
    static protected $staticProtectedStrings = array('One', 'Two', 'Three');
}