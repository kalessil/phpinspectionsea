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
    $x  = call_user_func(array('ClassVFChild', 'sum'), 1, 2, 3);          // <- reported
    $x .= call_user_func(array('ClassVFChild', 'sum'), 1, 2, 3);          // <- reported
    call_user_func(array('ClassVFChild', 'sum'), 1, 2, 3);                // <- reported

    /* Case, replace with -> call_user_func(..., 1, 2, 3) */
    call_user_func_array('ClassVFChild::sum',          array(1, 2, 3));   // <- reported
    call_user_func_array(array('ClassVFChild', 'sum'), array(1, 2, 3));   // <- reported

    call_user_func('abs', -1);                                            // <- reported, abs
    call_user_func('ClassVFChild::sum', 1, 2, 3);                         // <- reported, ClassVFParent::sum
    call_user_func(array('ClassVFChild', 'sum'), 1, 2, 3);                // <- reported, ClassVFParent::sum
    call_user_func(array('ClassVFChild', 'ClassVFParent::sum'), 1, 2, 3); // <- reported, ClassVFParent::sum
    call_user_func(array($object, 'sum'), 1, 2, 3);                       // <- reported, $object->sum
    call_user_func(array($object, 'ClassVFParent::sum'), 1, 2, 3);        // <- reported, ClassVFParent::sum

    /* false-positives: context and limitations */
    echo call_user_func('...');
    call_user_func(callReturningCallable()); // should be callReturningCallable()(), but PS not supporting the syntax
    /* false-positives: relative addressing */
    call_user_func(array('ClassVFChild', 'parent::sum'), 1, 2, 3);
    call_user_func(array($object, 'parent::sum'), 1, 2, 3);
