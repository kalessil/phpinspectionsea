<?php

if (1 > 4) {
    return 1;
} <weak_warning descr="Keyword else can be safely removed.">else</weak_warning> {
    return 5;
}

if (1 > 4) {
    if(5<6) {
        return 1;
    }
}  else {
    return 5;
}

if (1 > 4) {
    echo "test";
}  else {
    return 5;
}

if(true){
    die(1);
}<weak_warning descr="Keyword else can be safely removed.">else</weak_warning>{
    echo "Test";
}

if(true){
    exit;
}<weak_warning descr="Keyword else can be safely removed.">else</weak_warning>{
    echo "Test";
}

if(true){
    if(1){
        exit;
    }
}else{
    echo "Test";
}



if (1 > 4) {
    die(1);
} elseif (1 > 5) {
    return 2;
} elseif (1 > 5) {
    return 3;
}


if(true){
  throw new Exception("Test");
}<weak_warning descr="Keyword else can be safely removed.">else</weak_warning>{
    echo "Test";
}

foreach([1,2,3] as $b){
    if (1 > 4) {
        continue;
    }  <weak_warning descr="Keyword else can be safely removed.">else</weak_warning> {
        echo 123;
    }

}
foreach([1,2,3] as $b){
    if (1 > 4) {
        break;
    }  elseif(1>$b) {
        echo 123;
    }

}