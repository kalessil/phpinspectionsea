<?php

    /* basic cases: if-else, if-elseif-else */
    if ($x)     { $varIfElse = true;  }
    <weak_warning descr="Assignment in this branch shall be moved before if">else</weak_warning> { $varIfElse = false; }

    if ($x)     { $varIfElse = true;  }
    elseif ($y) { $varIfElse = true;  }
    <weak_warning descr="Assignment in this branch shall be moved before if">else</weak_warning> { $varIfElse = false; }

    /* base cases: default value types */
    if ($x)     { $var = true;  }
    <weak_warning descr="Assignment in this branch shall be moved before if">else</weak_warning> { $var = false; }

    if ($x)     { $var = true;  }
    <weak_warning descr="Assignment in this branch shall be moved before if">else</weak_warning> { $var = 5;     }
    if ($x)     { $var = true;  }
    <weak_warning descr="Assignment in this branch shall be moved before if">else</weak_warning> { $var = .5;    }

    if ($x)     { $var = true;      }
    <weak_warning descr="Assignment in this branch shall be moved before if">else</weak_warning> { $var = 'default'; }

    if ($x)     { $var = true;             }
    <weak_warning descr="Assignment in this branch shall be moved before if">else</weak_warning> { $var = $anotherVariable; }

    if ($x)     { $var = true; }
    <weak_warning descr="Assignment in this branch shall be moved before if">else</weak_warning> { $var = [];   }


    /* false-positives: not all variables are the same */
    if ($x)     { $var1 = true;  }
    elseif ($y) { $var2 = true;  }
    else        { $var1 = false; }

    /* false-positives: default values are of limited type */
    if ($x)     { $var = true;         }
    else        { $var = str_repeat(); }
