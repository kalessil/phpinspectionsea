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

        protected function methodDuplicate($x)
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

        protected function <weak_warning descr="'method' method can be dropped, as it identical to parent's one.">method</weak_warning> ()
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

        public function <weak_warning descr="'methodProxy' method should call parent's one instead of duplicating code.">methodProxy</weak_warning>($x)
        {
            return $this->method();
        }

        public function <weak_warning descr="'methodDuplicate' method should call parent's one instead of duplicating code.">methodDuplicate</weak_warning>($x)
        {
            return $this->method();
        }

        protected function abc()
        {
            echo 2;
        }

    }