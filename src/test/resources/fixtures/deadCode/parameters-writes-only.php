<?php

class Container {
    public function method1(array $in, array &$out, $log) {
        return
            function
                (array $inner, array &$outer)
                use (
                    <weak_warning descr="The variable seems to be not used.">$in</weak_warning>,
                    &<weak_warning descr="The variable seems to be not used.">$outUnused</weak_warning>,
                    &$outUsed,
                    $log
                )
            {
                <weak_warning descr="Parameter/variable is overridden, but is never used or appears outside of the scope.">$inner[]</weak_warning> = '';
                $outer []= '';
                $outUsed = 1;
                <weak_warning descr="Parameter/variable is overridden, but is never used or appears outside of the scope.">$log[]</weak_warning> = '';
            };
    }

    public function method2(array $in, array &$out) {
        <weak_warning descr="Parameter/variable is overridden, but is never used or appears outside of the scope.">$in[]</weak_warning> = '';
        $out []= '';
    }
}
