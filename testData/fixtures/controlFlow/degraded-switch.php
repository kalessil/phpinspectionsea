<?php

<weak_warning descr="Switch construct behaves as if-else, consider refactoring.">switch</weak_warning> ($what) {
    case 'one':
        break;
    default:
        break;
}
<weak_warning descr="Switch construct behaves as if, consider refactoring.">switch</weak_warning> ($what) {
    case 'one':
        break;
}
<weak_warning descr="Switch construct has default case only, consider leaving only the default case's body.">switch</weak_warning> ($what) {
    default:
        break;
}

switch ($what) {}

switch ($what) {
    case 'one':
    case 'two':
        break;
    default:
        break;
}