<?php

    class ClassMethodDefinedHere {

        protected function method() {
            // PsiEquivalenceUtil.areElementsEquivalent is not matching those assignments properly
            // We had to place ugly workaround there
            $x = [];
            $x = 1;

            echo rtrim('');
            return $x;
        }

        protected function abc(){
            echo 1;
        }

    }

    class ClassTransitive extends ClassMethodDefinedHere {}

    class ClassDuplicates extends ClassTransitive {

        protected function <weak_warning descr="'method' method can be dropped, as it identical to parent's one.">method</weak_warning> () {
            // PsiEquivalenceUtil.areElementsEquivalent is not matching those assignments properly
            // We had to place ugly workaround there
            $x = [];
            $x = 1;

            /* pollute method with comments on 1st nesting level */
            echo rtrim('');

            /** pollute method with doc-block on 1st nesting level */
            /** pollute method with doc-blocks on 1st nesting level */
            return $x;
        }

        protected function abc(){
            echo 2;
        }

    }