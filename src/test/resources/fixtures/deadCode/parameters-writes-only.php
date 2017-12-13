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
        }

        /* false-positives: by reference manipulations */
        $array = [0, 0];

        $local1 = &$array[0];
        $local1 = $array[1];

        $local2 = &$array[0];
        ++$local2;
    }

    public function method4() {
        $x = new UnknownClass($y = new ClassWithArrayAccess());
        $y[] = null;
    }

    public function method5() {
        return false === (<weak_warning descr="Parameter/variable is overridden, but is never used or appears outside of the scope.">$x</weak_warning> = array_search('', []));
    }
}