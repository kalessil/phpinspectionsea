<?php

class Container {
    public function method1(array $in, array &$out, $log) {
        return
            function
                (array $inner, array &$outer)
                use (
                    <weak_warning descr="The variable seems to be not used.">$in</weak_warning>,
                    &<weak_warning descr="The variable seems to be not used.">$out</weak_warning>,
                    $log
                )
            {
                <weak_warning descr="Parameter/variable is overridden, but is never used or appears outside of the scope.">$inner[]</weak_warning> = '';
                $outer []= '';
                <weak_warning descr="Parameter/variable is overridden, but is never used or appears outside of the scope.">$log[]</weak_warning> = '';
            };
    }

    public function method2(array $in, array &$out) {
        <weak_warning descr="Parameter/variable is overridden, but is never used or appears outside of the scope.">$in[]</weak_warning> = '';
        $out []= '';
    }

    public function method3($a, $b, $c, &$d) {
        $local = 0;

        foreach ([] as $i => $v) {
            <weak_warning descr="Parameter/variable is overridden, but is never used or appears outside of the scope.">$a[$i]</weak_warning> = ++$c;

            <weak_warning descr="Parameter/variable is overridden, but is never used or appears outside of the scope.">++$b</weak_warning>;
            <weak_warning descr="Parameter/variable is overridden, but is never used or appears outside of the scope.">$b++</weak_warning>;

            ++$d;

            $array = [0];
            $local = &$array[0];
            ++$local;
        }
    }
}