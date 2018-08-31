<?php

    /*
    TODO: (configurable) call_user_func's first parameter validation
    TODO: (configurable) amount of arguments validation for resolved methods/functions
    */

    class ClassVFParent {
        static public function sum($p1, $p2, $p3) { $sum = $p1+$p2+$p3; echo $sum.PHP_EOL; return $sum; }
    }
    class ClassVFChild extends ClassVFParent {
        static public function sum($p1, $p2, $p3) { $sum = 0; echo $sum.PHP_EOL; return $sum; }
    }
    $object = new ClassVFChild();

    /* cases with different context */
    $x  = <weak_warning descr="'ClassVFChild::sum(1, 2, 3)' should be used instead.">call_user_func (array('ClassVFChild', 'sum'), 1, 2, 3)</weak_warning>;
    $x .= <weak_warning descr="'ClassVFChild::sum(1, 2, 3)' should be used instead.">call_user_func (array('ClassVFChild', 'sum'), 1, 2, 3)</weak_warning>;
    echo   <weak_warning descr="'ClassVFChild::sum(1, 2, 3)' should be used instead.">call_user_func (array('ClassVFChild', 'sum'), 1, 2, 3)</weak_warning>;
    return <weak_warning descr="'ClassVFChild::sum(1, 2, 3)' should be used instead.">call_user_func (array('ClassVFChild', 'sum'), 1, 2, 3)</weak_warning>;
    <weak_warning descr="'ClassVFChild::sum(1, 2, 3)' should be used instead.">call_user_func (array('ClassVFChild', 'sum'), 1, 2, 3)</weak_warning>;

    /* Case, replace with -> call_user_func(..., 1, 2, 3) */
    <weak_warning descr="'call_user_func('ClassVFChild::sum', 1, 2, 3)' should be used instead (enables further analysis).">call_user_func_array ('ClassVFChild::sum', array(1, 2, 3))</weak_warning>;
    <weak_warning descr="'call_user_func(array('ClassVFChild', 'sum'), 1, 2, 3)' should be used instead (enables further analysis).">call_user_func_array (array('ClassVFChild', 'sum'), array(1, 2, 3))</weak_warning>;

    <weak_warning descr="'abs(-1)' should be used instead.">call_user_func ('abs', -1)</weak_warning>;
    <weak_warning descr="'ClassVFChild::sum(1, 2, 3)' should be used instead.">call_user_func ('ClassVFChild::sum', 1, 2, 3)</weak_warning>;
    <weak_warning descr="'ClassVFChild::sum(1, 2, 3)' should be used instead.">call_user_func (array('ClassVFChild', 'sum'), 1, 2, 3)</weak_warning>;
    <weak_warning descr="'ClassVFParent::sum(1, 2, 3)' should be used instead.">call_user_func (array('ClassVFChild', 'ClassVFParent::sum'), 1, 2, 3)</weak_warning>;
    <weak_warning descr="'$object->sum(1, 2, 3)' should be used instead.">call_user_func (array($object, 'sum'), 1, 2, 3)</weak_warning>;
    <weak_warning descr="'ClassVFParent::sum(1, 2, 3)' should be used instead.">call_user_func (array($object, 'ClassVFParent::sum'), 1, 2, 3)</weak_warning>;

    /* first is string without injections or variable */
    <weak_warning descr="'$object->{\"sum{$i}\"}(1, 2, 3)' should be used instead.">call_user_func (array($object, "sum{$i}"), 1, 2, 3)</weak_warning>;
    <weak_warning descr="'$object->$i(1, 2, 3)' should be used instead.">call_user_func (array($object, $i), 1, 2, 3)</weak_warning>;
