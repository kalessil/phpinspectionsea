<?php
    interface classContract {
        static function __construct(&$arg1, &$arg2, &$arg3);
        static function __clone(&$arg1, &$arg2, &$arg3);
        static function __destruct(&$arg1, &$arg2, &$arg3);
    }

    /** @noinspection SingletonFactoryPatternViolationInspection */
    class classWithMagicMethods implements classContract {
        /** @noinspection PhpHierarchyChecksInspection */
        /* name (1) and return (2) are highlighted */
        static protected function __construct(&$arg1, &$arg2, &$arg3) {
            if (1) return ''; else return '';
        }

        /** @noinspection PhpHierarchyChecksInspection */
        /* name (2) and return (2) are highlighted */
        static protected function __clone(&$arg1, &$arg2, &$arg3)     {
            if (1) return ''; else return '';
        }
        /** @noinspection PhpHierarchyChecksInspection */
        /* name (2) and return (2) are highlighted */
        static protected function __destruct(&$arg1, &$arg2, &$arg3)  {
            if (1) return ''; else return '';
        }

        /* names (4) are highlighted */
        static protected function __get(&$arg1, &$arg2, &$arg3)   {}
        static protected function __isset(&$arg1, &$arg2, &$arg3) {}
        static protected function __unset(&$arg1, &$arg2, &$arg3) {}

        /* names (4) are highlighted */
        static protected function __set(&$arg1, &$arg2, &$arg3)  {}
        static protected function __call(&$arg1, &$arg2, &$arg3) {}

        /* name (4) is highlighted */
        protected function __callStatic(&$arg1, &$arg2, &$arg3) {}

        /* name (3) throw (2) return (1) are highlighted */
        static protected function __toString(&$arg1, &$arg2, &$arg3) {
            if (1)     throw new \Exception('E1');
            elseif (2) throw new \Exception('E2');
            elseif (3) return null;
            elseif (4) return (string) null;
            else       return implode(array(), '');
        }

        /* name (3) return (1) are highlighted */
        static protected function __debugInfo(&$arg1, &$arg2, &$arg3) {
            if (1)     return null;
            elseif (2) return (string) null;
            else       return array();
        }

        /* name (2) is highlighted */
        static protected function __invoke(&$arg1, &$arg2, &$arg3) {}

        /* name (1) is highlighted, but (2) inspected */
        static protected function __autoload(&$arg1, &$arg2, &$arg3) {}
    }