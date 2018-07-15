<?php

    <error descr="The call doesn't validate protocol and vulnerable to Java Script injections and Arbitrary files loading.">filter_var('...', FILTER_VALIDATE_URL)</error>;

    <error descr="Because of missing '^' at the beginning, it's possible to bypass the validation (e.g. 'URL?whatever=http://...').">preg_match('#\bhttp://#', '...')</error>;
    <error descr="Because of missing '^' at the beginning, it's possible to bypass the validation (e.g. 'URL?whatever=http://...').">preg_match('#http://#', '...')</error>;
    <error descr="Because of missing '^' at the beginning, it's possible to bypass the validation (e.g. 'URL?whatever=http://...').">preg_match('#(https?://)#', '...')</error>;
    <error descr="Because of missing '^' at the beginning, it's possible to bypass the validation (e.g. 'URL?whatever=http://...').">preg_match('#(?:https?://)#', '...')</error>;

    preg_match('#^http://#', '...');

    <error descr="Because of missing '$' at the end, it's possible to bypass the validation (e.g. 'file.jpg.php').">preg_match('#\.jpg#', '...')</error>;
    <error descr="Because of missing '$' at the end, it's possible to bypass the validation (e.g. 'file.jpg.php').">preg_match('#\.(jpg)#', '...')</error>;
    <error descr="Because of missing '$' at the end, it's possible to bypass the validation (e.g. 'file.jpg.php').">preg_match('#\.(jpe?g)#', '...')</error>;
    <error descr="Because of missing '$' at the end, it's possible to bypass the validation (e.g. 'file.jpg.php').">preg_match('#\.(jpg|png)#', '...')</error>;
    <error descr="Because of missing '$' at the end, it's possible to bypass the validation (e.g. 'file.jpg.php').">preg_match('#\.(?:jpg|png)#', '...')</error>;
    <error descr="Because of missing '$' at the end, it's possible to bypass the validation (e.g. 'file.jpg.php').">preg_match('#\.(?:jpg|png)?#', '...')</error>;

    preg_match('#\.jpg$#', '...');
    preg_match('#.jpg#', '...');
