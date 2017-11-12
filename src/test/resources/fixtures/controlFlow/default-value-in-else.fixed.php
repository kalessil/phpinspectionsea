<?php

function cases_holder() {
    /* basic cases: if-else, if-elseif-else */
    $varIfElse = false;
    if ($x)     { $varIfElse = true;  }

    $varIfElse = false;
    if ($x)     { $varIfElse = true;  }
    elseif ($y) { $varIfElse = true;  }

    /* base cases: default value types */
    $var = false;
    if ($x)     { $var = true;  }

    $var = 5;
    if ($x)     { $var = true;  }
    $var = .5;
    if ($x)     { $var = true;  }

    $var = 'default';
    if ($x)     { $var = true;      }

    $var = $anotherVariable;
    if ($x)     { $var = true;             }

    $var = [];
    if ($x)     { $var = true; }


    /* false-positives: not all variables are the same */
    if ($x)     { $var1 = true;  }
    elseif ($y) { $var2 = true;  }
    else        { $var1 = false; }

    /* false-positives: default values are of limited type */
    if ($x)     { $var = true;         }
    else        { $var = str_repeat(); }

    /* false-positive: the container used in a condition */
    if ($x)        { $var1 = true;  }
    elseif ($var1) { $var1 = true;  }
    else           { $var1 = false; }
    /* false-positive: the container used in a condition */
    if (!$var1)    { $var1 = true;  }
    else           { $var1 = false; }
}