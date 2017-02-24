<?php

use BaseT as AliasT; //Add alias to cover all test cases

class BaseT
{

    public static $userId = 1;
    protected static $userName = 'Test';

    public function a()
    {

        self::$userId = 1; // Ok
        AliasT::$userId = 1;  // OK
        BaseT::$userId = 1; // Ok
        \BaseT::$userId = 1; // Ok
        BaseT::$userName = 'Name'; // Ok
        self::$userName = 'Name'; // Ok



        <warning descr="Static property should be modified only inside the source class">static::$userId = 1</warning>;
        <warning descr="Static property should be modified only inside the source class">static::$userName = 'Name'</warning>;

        return function () {
            <warning descr="Static property should be modified only inside the source class">AliasT::$userId = 123</warning>;  // Error
        };
    }

}

class ExtendedT extends BaseT
{

    public static $userName = 'ExtendedTest';

    public function dfTest()
    {
        ExtendedT::$userName = 'Test'; // Ok

        <warning descr="Static property should be modified only inside the source class">AliasT::$userId = 4333</warning>;
        <warning descr="Static property should be modified only inside the source class">ExtendedT::$userId = 4333</warning>;

    }
}

class CustomClass
{
    public function test()
    {
       <warning descr="Static property should be modified only inside the source class">\BaseT::$userId = 123</warning>;
       <warning descr="Static property should be modified only inside the source class">AliasT::$userId = 123</warning>;
       <warning descr="Static property should be modified only inside the source class">a::$userId = 123</warning>;
    }
}

class A {

    public function test(){
        <warning descr="Static property should be modified only inside the source class">A::$test = 123</warning>;
    }
}


<warning descr="Static property should be modified only inside the source class">BaseT::$userId = 123</warning>;
<warning descr="Static property should be modified only inside the source class">AliasT::$userId = 123</warning>;
<warning descr="Static property should be modified only inside the source class">ExtendedT::$userId = 123</warning>;
<warning descr="Static property should be modified only inside the source class">\ExtendedT::$userId = 123</warning>;
<warning descr="Static property should be modified only inside the source class">CustomClassName::$test = 123</warning>;