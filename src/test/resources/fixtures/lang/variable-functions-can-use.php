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
    $x  = <weak_warning descr="'ClassVFChild::sum(1, 2, 3)' should be used instead">call_user_func</weak_warning> (array('ClassVFChild', 'sum'), 1, 2, 3);
    $x .= <weak_warning descr="'ClassVFChild::sum(1, 2, 3)' should be used instead">call_user_func</weak_warning> (array('ClassVFChild', 'sum'), 1, 2, 3);
    echo   <weak_warning descr="'ClassVFChild::sum(1, 2, 3)' should be used instead">call_user_func</weak_warning> (array('ClassVFChild', 'sum'), 1, 2, 3);
    return <weak_warning descr="'ClassVFChild::sum(1, 2, 3)' should be used instead">call_user_func</weak_warning> (array('ClassVFChild', 'sum'), 1, 2, 3);
    <weak_warning descr="'ClassVFChild::sum(1, 2, 3)' should be used instead">call_user_func</weak_warning> (array('ClassVFChild', 'sum'), 1, 2, 3);

    /* Case, replace with -> call_user_func(..., 1, 2, 3) */
    <weak_warning descr="'call_user_func('ClassVFChild::sum', 1, 2, 3)' should be used instead (enables further analysis)">call_user_func_array</weak_warning> ('ClassVFChild::sum', array(1, 2, 3));
    <weak_warning descr="'call_user_func(array('ClassVFChild', 'sum'), 1, 2, 3)' should be used instead (enables further analysis)">call_user_func_array</weak_warning> (array('ClassVFChild', 'sum'), array(1, 2, 3));

    <weak_warning descr="'abs(-1)' should be used instead">call_user_func</weak_warning> ('abs', -1);
    <weak_warning descr="'ClassVFChild::sum(1, 2, 3)' should be used instead">call_user_func</weak_warning> ('ClassVFChild::sum', 1, 2, 3);
    <weak_warning descr="'ClassVFChild::sum(1, 2, 3)' should be used instead">call_user_func</weak_warning> (array('ClassVFChild', 'sum'), 1, 2, 3);
    <weak_warning descr="'ClassVFParent::sum(1, 2, 3)' should be used instead">call_user_func</weak_warning> (array('ClassVFChild', 'ClassVFParent::sum'), 1, 2, 3);
    <weak_warning descr="'$object->sum(1, 2, 3)' should be used instead">call_user_func</weak_warning> (array($object, 'sum'), 1, 2, 3);
    <weak_warning descr="'ClassVFParent::sum(1, 2, 3)' should be used instead">call_user_func</weak_warning> (array($object, 'ClassVFParent::sum'), 1, 2, 3);

    /* first is string without injections or variable */
    <weak_warning descr="'$object->{\"sum{$i}\"}(1, 2, 3)' should be used instead">call_user_func</weak_warning> (array($object, "sum{$i}"), 1, 2, 3);
    <weak_warning descr="'$object->$i(1, 2, 3)' should be used instead">call_user_func</weak_warning> (array($object, $i), 1, 2, 3);

    /* false-positives: callback analysis limitations */
    call_user_func ("$class::sum", 1, 2, 3);
    call_user_func (array($classes[$i], "sum"), 1, 2, 3);
    call_user_func (array($classes->i, "sum"), 1, 2, 3);
    /* false-positives: IDE limitations */
    call_user_func (callReturningCallable()); // should be callReturningCallable()(), but PS not supporting the syntax
    /* false-positives: relative addressing */
    call_user_func (array('ClassVFChild', 'parent::sum'), 1, 2, 3);
    call_user_func (array($object, 'parent::sum'), 1, 2, 3);
