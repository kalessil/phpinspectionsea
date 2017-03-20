<?php

/* pattern: exist expressions */
if ($condition) { die; }  <weak_warning descr="'else' is not needed here (because of the last statement in if-branch).">else</weak_warning> { ; }
if ($condition) { exit; } <weak_warning descr="'else' is not needed here (because of the last statement in if-branch).">else</weak_warning> { ; }

/* pattern: return point expressions */
if ($condition) { return; }   <weak_warning descr="'else' is not needed here (because of the last statement in if-branch).">else</weak_warning> { ; }
if ($condition) { throw $e; } <weak_warning descr="'else' is not needed here (because of the last statement in if-branch).">else</weak_warning> { ; }
for (;;) {
    if ($condition) { break; }    <weak_warning descr="'else' is not needed here (because of the last statement in if-branch).">else</weak_warning> { ; }
    if ($condition) { continue; } <weak_warning descr="'else' is not needed here (because of the last statement in if-branch).">else</weak_warning> { ; }
}

/* pattern: alternative branches invariants */
if ($condition) { return; }
    <weak_warning descr="'else' is not needed here (because of the last statement in if-branch).">else</weak_warning> { ; }
if ($condition) { return; }
    <weak_warning descr="'elseif' is not needed here (because of the last statement in if-branch).">elseif</weak_warning> ($condition)  { ; }
if ($condition) { return; }
    <weak_warning descr="'elseif' is not needed here (because of the last statement in if-branch).">elseif</weak_warning> ($condition)  { ; }  elseif($condition) { ; }
if ($condition) { return; }
    <weak_warning descr="'else' is not needed here (because of the last statement in if-branch).">else</weak_warning> if ($condition) { ; }
if ($condition) { return; }
    <weak_warning descr="'else' is not needed here (because of the last statement in if-branch).">else</weak_warning> if ($condition) { ; } else { ; }

/* false-positives: alternative syntax */
if ($condition):
    return;
else:
    ;
endif;

/* false-positives: else-context */
if ($condition) { ; } else if ($condition) { return; } else { ; }

/* false-positives: structure not as expected */
if ($condition) { ; }       else { ; }
if ($condition) {}          else {}
if ($condition) return;     else ;
if ($condition) ;           else return;
if ($condition) { return; } else ;
if ($condition) { return; } elseif ($condition) ;