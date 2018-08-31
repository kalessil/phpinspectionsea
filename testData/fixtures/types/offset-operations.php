<?php

    class OffsetSupport {
        /**
         * @param string $strIndex
         * @return string
         */
        public function offsetGet($strIndex) {
            return '';
        }
        /**
         * @param string $strIndex
         * @param string $strValue
         * @return string
         */
        public function offsetSet($strIndex, $strValue) {
            return '';
        }
    }

    class MagicSupport {
        public function __get($strIndex) {
            return '';
        }
    }

    class TestContainer
    {
        public function flow() {
            $strContainer = '';
            echo $strContainer{0};
            $strContainer[0] = 'a';
            $strContainer{0} = 'a';
            $strContainer
                [<error descr="Resolved index type ([array]) is incompatible with possible [string, int]. Probably just proper type hinting needed.">explode('', '')</error>] = 'a';
            $strContainer[]= 'a';

            $arrContainer = array();
            $arrContainer[0] = 'a';
            $arrContainer
                [<error descr="Resolved index type ([array]) is incompatible with possible [string, int]. Probably just proper type hinting needed.">explode('', '')</error>] = 'a';
            $arrContainer[]= 'a';

            $boolContainer = false;
            <error descr="'$boolContainer' may not support offset operations (or its type not annotated properly: [bool]).">$boolContainer[0]</error>
                = 'a';
            <error descr="'$boolContainer' may not support offset operations (or its type not annotated properly: [bool]).">$boolContainer[explode('', '')]</error>
                = 'a';

            $objOffsetContainer = new OffsetSupport();
            $objOffsetContainer[''] = 0;
            $objOffsetContainer[<error descr="Resolved index type ([int]) is incompatible with possible [string]. Probably just proper type hinting needed.">0</error>]
                = 0;
            $objOffsetContainer
                [<error descr="Resolved index type ([\stdClass]) is incompatible with possible [string]. Probably just proper type hinting needed.">new stdClass()</error>] = 0;

            $objMagicContainer = new MagicSupport();
            $objMagicContainer[0] = 0;
            $objMagicContainer
                [<error descr="Resolved index type ([\stdClass]) is incompatible with possible [string, int]. Probably just proper type hinting needed.">new stdClass()</error>] = 0;

            $objStdContainer = new stdClass();
            <error descr="'$objStdContainer' may not support offset operations (or its type not annotated properly: [\stdClass]).">$objStdContainer[0]</error>
                = 0;
            <error descr="'$objStdContainer' may not support offset operations (or its type not annotated properly: [\stdClass]).">$objStdContainer[new stdClass()]</error>
                = 0;

            /* TODO: this needs to be reported */
            $objTestObjectContainer   = new PDO('','','');
            $objTestObjectContainer[] = "This is a message";
        }
    }

    function false_positives_holder() {
        foreach ([] as $key => $value) {} /* -> string|integer */
        $string = substr('', -1);         /* -> string|false   */

        return [
            $key[0],
            $string[0],
        ];
    }

    /** @param array[] $array */
    function false_positives_array_type_correction(array $array) {
        foreach ($array as $key => $value) {
            $array[$key + 1]['...'] = null;
            $array[1 + $key + 1]['...'] = null;
        }
    }
