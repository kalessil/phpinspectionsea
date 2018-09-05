<?php

function cases_holder(){
    foreach ([] as $collection) {
        $result = <error descr="'array_merge(...)' is used in a loop and is a resources greedy construction.">array_merge($result, $collection)</error>;
        $result = <error descr="'array_merge_recursive(...)' is used in a loop and is a resources greedy construction.">array_merge_recursive($result, $collection)</error>;
        $result = <error descr="'array_replace(...)' is used in a loop and is a resources greedy construction.">array_replace($result, $collection)</error>;
        $result = <error descr="'array_replace_recursive(...)' is used in a loop and is a resources greedy construction.">array_replace_recursive($result, $collection)</error>;
    }

    $result = array_merge($result, $collection);

    foreach ([] as $collection) {
        $result = array_merge($result, $collection);
        $result = array_merge($result, $collection);
        break;
    }

    foreach ([] as $collection) {
        $result = array_merge($result, $collection);
        $result = array_merge($result, $collection);
        return;
    }
}
