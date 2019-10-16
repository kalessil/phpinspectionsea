<?php

function cases_holder() {
    <error descr="[EA] A race condition can corrupt the file content. It would be a good idea to use LOCK_EX flag.">file_put_contents('.php', '')</error>;
    <error descr="[EA] A race condition can corrupt the file content. It would be a good idea to use LOCK_EX flag.">file_put_contents('' . '.php', '')</error>;

    <error descr="[EA] A race condition can corrupt the file content. It would be a good idea to use LOCK_EX flag.">file_put_contents('.php', '<?php')</error>;
    <error descr="[EA] A race condition can corrupt the file content. It would be a good idea to use LOCK_EX flag.">file_put_contents('.php', '<?php' . '')</error>;

    $file = '*' . '.php';
    $content = '<?php' . '';
    <error descr="[EA] A race condition can corrupt the file content. It would be a good idea to use LOCK_EX flag.">file_put_contents($file, '')</error>;
    <error descr="[EA] A race condition can corrupt the file content. It would be a good idea to use LOCK_EX flag.">file_put_contents('', $content)</error>;

    file_put_contents('', '');
}