<?php


class BaseClass
{

    public static $userId = 1;
    protected static $userName = 'Test';
    protected static $users = [];

    public function a()
    {

        foreach (self::$users as $user) {

        }

        <warning descr="Static property should not be modified">self::$userId = 1</warning>;
        <warning descr="Static property should not be modified">BaseClass::$userId = 1</warning>;

        echo self::$userId;



        <warning descr="Static property should not be modified">static::$userId = 1</warning>;

        return function () {
            <warning descr="Static property should not be modified">AliasT::$userId = 123</warning>;
        };
    }

}


<warning descr="Static property should not be modified">BaseClass::$userId = 123</warning>;
<warning descr="Static property should not be modified">CustomClassName::$test = 123</warning>;

echo BaseClass::$userId;