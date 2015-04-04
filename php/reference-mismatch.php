<?php

    function parameterUsageInAssignments(&$a) {
        /* reference mismatch, copy stored */
        $y = $a;
        $y []= '1'; //local copy modification
        $a []= '2'; //origin modification

        $z = &$a;
        /* legal */
        array_push($z, 'value');
        /* reference mismatch */
        $z = in_array('value', $z, true);

        return $y;
    }

    function foreachValueReference() {
        $types = array();
        foreach ($types as $index => & $type) {
            /* reference mismatch, copy supplied anyway */
            $type = in_array('something', $type, true);
        }

        return $types;
    }

    function process(&$parameter) {
        /* legal */
        array_push($parameter, 'value');
        /* reference mismatch */
        return in_array('value', $parameter, true);
    }

    function & collector() {
        static $collection = array();
        return $collection;
    }

    /* illegal, in spite of reference returned, copy will be obtained */
    $collection = collector();  $collection[] = '1';
    /* legal, reference preserved */
    $collection = &collector(); $collection[] = '2';

    /* "... = &..."          => can introduce mismatches */
    /* " foreach (... &...)" => can introduce mismatches */