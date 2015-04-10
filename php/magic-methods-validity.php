<?php
    interface classContract {
        static function __construct(&$arg1, &$arg2, &$arg3);
        static function __clone(&$arg1, &$arg2, &$arg3);
        static function __destruct(&$arg1, &$arg2, &$arg3);
    }

    /** @noinspection SingletonFactoryPatternViolationInspection */
    class classWithMagicMethods implements classContract {
        /** @noinspection PhpHierarchyChecksInspection */
        /* name (1) and return (1) are highlighted */
        static protected function __construct(&$arg1, &$arg2, &$arg3)
        {
            return '';
        }

        /** @noinspection PhpHierarchyChecksInspection */
        /* name (2) and return (1) are highlighted */
        static protected function __clone(&$arg1, &$arg2, &$arg3)
        {
            return '';
        }
        /** @noinspection PhpHierarchyChecksInspection */
        /* name (2) and return (1) are highlighted */
        static protected function __destruct(&$arg1, &$arg2, &$arg3)
        {
            return '';
        }
    }