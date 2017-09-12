<?php

    class ClassMethodDefinedHere {

        protected function method()
        {
            // single-line comment here
            $x = [];
            $x = 1;

            echo rtrim('');
            return $x;
        }

        protected function methodProxy($x)
        {
            return $this->method();
        }

        private function methodDuplicate($x)
        {
            return $this->method();
        }

        protected function abc()
        {
            echo 1;
        }

    }

    class ClassTransitive extends ClassMethodDefinedHere {}

    class ClassDuplicates extends ClassTransitive {

        public function methodProxy($x)
        {
            return parent::methodProxy($x);
        }

        public function methodDuplicate($x)
        {
            return $this->method();
        }

        protected function abc()
        {
            echo 2;
        }

    }