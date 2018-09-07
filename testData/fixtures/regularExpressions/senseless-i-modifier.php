<?php

    preg_match(<weak_warning descr="'i' modifier is ambiguous here (no alphabet characters in given pattern).">'/123.+/i'</weak_warning>, '');

    preg_match(<error descr="'а-я' range in '[а-яё]' is looking rather suspicious, please check.">'/[а-яё].+/iu'</error>, '');
    preg_match('/abc.+/i', '');