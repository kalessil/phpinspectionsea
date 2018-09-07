<?php

    /* case: contains unicode characters */
    preg_match(<error descr="/u modifier is missing (unicode characters found).">'/[а-яё].+/'</error>, '');
    preg_match(<error descr="/u modifier is missing (unicode characters found).">'/[а-яё].+/i'</error>, '');
    preg_match(<error descr="'а-я' range in '[а-яё]' is looking rather suspicious, please check.">'/[а-яё].+/u'</error>, '');
    preg_match('/abc.+/',     '');

    /* case: contains \p, \P, \X */
    preg_match(<error descr="/u modifier is missing (unicode codepoints found).">'/\p/'</error>, '');
    preg_match(<error descr="/u modifier is missing (unicode codepoints found).">'/\P/'</error>, '');
    preg_match(<error descr="/u modifier is missing (unicode codepoints found).">'/\X/'</error>, '');
    preg_match('/\p/u', '');
    preg_match('/\\p/', '');