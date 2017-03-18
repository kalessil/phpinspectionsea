<?php


if(true){
    exit;
}<weak_warning descr="Else is not needed here, due to the last statement in previous branch.">else</weak_warning> if(1>4){
    echo "Test";
}

if(true){
    if(1){
        exit;
    }
}else{
    echo "Test";
}


if (1>0):
    return 1;
<weak_warning descr="Else is not needed here, due to the last statement in previous branch.">elseif</weak_warning>(2>0):
    return 2;
else:
    return 3;
endif;

if (1 > 4) {
    die(1);
} <weak_warning descr="Else is not needed here, due to the last statement in previous branch.">elseif</weak_warning> (1 > 5) {
    return 2;
} elseif (1 > 5) {
    return 3;
}



foreach([1,2,3] as $b){
    if (1 > 4) {
        continue;
    }  <weak_warning descr="Else is not needed here, due to the last statement in previous branch.">elseif</weak_warning> (1>4) {
        echo 123;
    }

}
foreach([1,2,3] as $b){
    if (1 > 4) {
        break;
    }  <weak_warning descr="Else is not needed here, due to the last statement in previous branch.">elseif</weak_warning>(1>$b) {
        echo 123;
    }

}