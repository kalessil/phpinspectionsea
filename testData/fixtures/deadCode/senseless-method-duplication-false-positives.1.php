<?php

namespace External\Óne {
    class ExistingClazz {}
}
namespace External\Two {
    class ExistingClazz {}
}

namespace MethodsDuplication\One {
    use External\Óne\ExistingClazz;
    use External\Óne\MissingClazz;

    const SMD_CONST = 1;
    function smd_func() {}

    class FirstClass
    {
        public function resolve_const()
        {
            return SMD_CONST;
        }
        public function resolve_function()
        {
            return smd_func();
        }
        public function resolve_existing_class()
        {
            return new ExistingClazz();
        }
        public function resolve_missing_class()
        {
            return new MissingClazz();
        }

        private function privateDuplicate() {
            return [];
        }

        protected function protectedDuplicateUsingPrivate() {
            return $this->privateDuplicate();
        }
    }
}


namespace MethodsDuplication\Two {
    use External\Two\ExistingClazz;
    use External\Two\MissingClazz;

    const SMD_CONST = 1;
    function smd_func() {}

    class SecondClass extends \MethodsDuplication\One\FirstClass
    {
        public function resolve_const()
        {
            return SMD_CONST;
        }
        public function resolve_function()
        {
            return smd_func();
        }
        public function resolve_existing_class()
        {
            return new ExistingClazz();
        }
        public function resolve_missing_class()
        {
            return new MissingClazz();
        }

        private function privateDuplicate() {
            return [];
        }

        protected function protectedDuplicateUsingPrivate() {
            return $this->privateDuplicate();
        }
    }
}
