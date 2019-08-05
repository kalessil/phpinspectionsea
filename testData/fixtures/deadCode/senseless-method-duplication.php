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

        protected function <weak_warning descr="'traitMethodDuplicate' method can be dropped, as it identical to '\TraitWithSameMethods::traitMethodDuplicate'.">traitMethodDuplicate</weak_warning>()
        {
            return $this->classMethodDuplicate();
        }
        protected function <weak_warning descr="'classMethodDuplicate' method can be dropped, as it identical to '\ClassWithSameMethods::classMethodDuplicate'.">classMethodDuplicate</weak_warning> ()
        {
            // single-line comment here
            $x = [];
            $x = 1;

            /* pollute method with comments on 1st nesting level */
            echo rtrim('');

            /** pollute method with doc-block on 1st nesting level */
            /** pollute method with doc-blocks on 1st nesting level */
            return $x;
        }

        public function <weak_warning descr="'protectedMethodProxy' method should call parent's one instead of duplicating code.">protectedMethodProxy</weak_warning>($x) { return $this->classMethodDuplicate(); }
        public function privateMethodProxy($x) { return $this->classMethodDuplicate(); }
        protected function abc() { echo 2; }
    }