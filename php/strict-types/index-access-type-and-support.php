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
            $strContainer[0] = 'a';
            $strContainer[explode('', '')] = 'a';
            $strContainer []= 'a';

            $arrContainer = array();
            $arrContainer[0] = 'a';
            $arrContainer[explode('', '')] = 'a';

            $boolContainer = false;
            $boolContainer[0] = 'a';
            $boolContainer[explode('', '')] = 'a';

            $objOffsetContainer = new OffsetSupport();
            $objOffsetContainer[0] = 0;
            $objOffsetContainer[new stdClass()] = 0;

            $objMagicContainer = new MagicSupport();
            $objMagicContainer[0] = 0;
            $objMagicContainer[new stdClass()] = 0;

            $objStdContainer = new stdClass();
            $objStdContainer[0] = 0;
            $objStdContainer[new stdClass()] = 0;

            $objTestObjectContainer = new PDO('','','');
            $objTestObjectContainer[]
                = "This is a message";
        }
    }
