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
            echo <weak_warning descr="Using [ ] instead of { } makes possible to analyze this expression">$strContainer{0}</weak_warning>;
            $strContainer[0] = 'a';
            <weak_warning descr="Using [ ] instead of { } makes possible to analyze this expression">$strContainer{0}</weak_warning> = 'a';
            $strContainer
                [<error descr="Resolved index type ([array]) is incompatible with possible [string, int]. Probably just proper type hinting needed.">explode('', '')</error>] = 'a';
            $strContainer[]= 'a';

            $arrContainer = array();
            $arrContainer[0] = 'a';
            $arrContainer
                [<error descr="Resolved index type ([array]) is incompatible with possible [string, int]. Probably just proper type hinting needed.">explode('', '')</error>] = 'a';
            $arrContainer[]= 'a';

            $boolContainer = false;
            <error descr="'$boolContainer' may not support offset operations (or it's type not annotated properly: [bool])">$boolContainer[0]</error>
                = 'a';
            <error descr="'$boolContainer' may not support offset operations (or it's type not annotated properly: [bool])">$boolContainer[explode('', '')]</error>
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
            <error descr="'$objStdContainer' may not support offset operations (or it's type not annotated properly: [\stdClass])">$objStdContainer[0]</error>
                = 0;
            <error descr="'$objStdContainer' may not support offset operations (or it's type not annotated properly: [\stdClass])">$objStdContainer[new stdClass()]</error>
                = 0;

            $objTestObjectContainer = new PDO('','','');
            <error descr="'$objTestObjectContainer' may not support offset operations (or it's type not annotated properly: [\PDO])">$objTestObjectContainer[]</error>
                = "This is a message";
        }
    }
