<?php

    trait TraitWithSameMethods {
        protected function traitMethodDuplicate() { return $this->classMethodDuplicate(); }
    }

    class ClassWithSameMethods {
        protected function classMethodDuplicate()
        {
            // single-line comment here
            $x = [];
            $x = 1;

            echo rtrim('');
            return $x;
        }

        protected function protectedMethodProxy($x) { return $this->classMethodDuplicate(); }
        private function privateMethodProxy($x) { return $this->classMethodDuplicate(); }
        protected function abc() { echo 1; }
    }

    class ClassDuplicates extends ClassWithSameMethods {
        use TraitWithSameMethods;

        public function protectedMethodProxy($x)
        {
            return parent::protectedMethodProxy($x);
        }
        public function privateMethodProxy($x) { return $this->classMethodDuplicate(); }
        protected function abc() { echo 2; }
    }