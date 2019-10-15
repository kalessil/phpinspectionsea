<?php

    /* case: contains unicode characters */
    preg_match(<error descr="[EA] /u modifier is missing (unicode characters found).">'/[а-яё].+/'</error>, '');
    preg_match(<error descr="[EA] /u modifier is missing (unicode characters found).">'/[а-яё].+/i'</error>, '');
    preg_match('/[а-яё].+/u', '');
    preg_match('/abc.+/',     '');
    preg_quote('/[а-яё].+/', '/');

    /* case: contains \p, \P, \X */
    preg_match(<error descr="[EA] /u modifier is missing (unicode codepoints found).">'/\p/'</error>, '');
    preg_match(<error descr="[EA] /u modifier is missing (unicode codepoints found).">'/\P/'</error>, '');
    preg_match(<error descr="[EA] /u modifier is missing (unicode codepoints found).">'/\X/'</error>, '');
    preg_match('/\p/u', '');
    preg_match('/\\p/', '');