<?php

namespace MethodsDuplication\One {
    const SMD_CONST = 1;
    function smd_func() {}

    class FirstClass
    {
        public function resolve()
        {
            return [SMD_CONST, smd_func()];
        }

        private function privateDuplicate() {
            return [];
        }
    }
}


namespace MethodsDuplication\Two {
    const SMD_CONST = 1;
    function smd_func() {}

    class SecondClass extends \MethodsDuplication\One\FirstClass
    {
        public function resolve()
        {
            return [SMD_CONST, smd_func()];
        }

        private function privateDuplicate() {
            return [];
        }
    }
}
