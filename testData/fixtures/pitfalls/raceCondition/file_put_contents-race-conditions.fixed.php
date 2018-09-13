<?php

function cases_holder() {
    file_put_contents('.php', '', LOCK_EX);
    file_put_contents('' . '.php', '', LOCK_EX);

    file_put_contents('.php', '<?php', LOCK_EX);
    file_put_contents('.php', '<?php' . '', LOCK_EX);

    $file = '*' . '.php';
    $content = '<?php' . '';
    file_put_contents($file, '', LOCK_EX);
    file_put_contents('', $content, LOCK_EX);

    file_put_contents('', '');
}