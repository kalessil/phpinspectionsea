<?php

    class ClassMethodDefinedHere {
        protected function method() {
            $x = [];
            return $x;
        }
    }
    class ClassTransitive extends ClassMethodDefinedHere {}
    class ClassDuplicates extends ClassTransitive {
        protected function <weak_warning descr="'method' method can be dropped, as it identical to parent's one">method</weak_warning> () {
            /* pollute method with comments on 1st nesting level */
            $x = [];
            /** pollute method with doc-block on 1st nesting level */
            /** pollute method with doc-blocks on 1st nesting level */
            return $x;
        }
    }