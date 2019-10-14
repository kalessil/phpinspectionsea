<?php

function cases_holder(){
    foreach ([] as $collection) {
        $result = <error descr="[EA] 'array_merge(...)' is used in a loop and is a resources greedy construction.">array_merge($result, $collection)</error>;
        $result = <error descr="[EA] 'array_merge_recursive(...)' is used in a loop and is a resources greedy construction.">array_merge_recursive($result, $collection)</error>;
        $result = <error descr="[EA] 'array_replace(...)' is used in a loop and is a resources greedy construction.">array_replace($result, $collection)</error>;
        $result = <error descr="[EA] 'array_replace_recursive(...)' is used in a loop and is a resources greedy construction.">array_replace_recursive($result, $collection)</error>;

        /* false-positives: multiple targets patching */
        $result['...'] = array_merge($result['...'], $collection);
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
