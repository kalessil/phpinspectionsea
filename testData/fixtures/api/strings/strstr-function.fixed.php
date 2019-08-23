<?php

    if (strpos('where', 'what') !== false)  ;
    if (strpos('where', 'what') === false) ;

    if ('where' || strpos('where', 'what') !== false) ;
    if ('where' && strpos('where', 'what') !== false) ;
    if ('where' or strpos('where', 'what') !== false) ;
    if ('where' and strpos('where', 'what') !== false) ;

    if (strpos('where', 'what') === false) ;
    if (strpos('where', 'what') !== false) ;
    if (strpos('where', 'what') === false)  ;
    if (strpos('where', 'what') !== false)  ;

    if (strpos('where', 'what') === false) ;
    if (strpos('where', 'what') !== false) ;
    if (strpos('where', 'what') === false) ;
    if (strpos('where', 'what') !== false) ;

    if (stripos('where', 'what') === false) ;
    if (stripos('where', 'what') !== false) ;
    if (stripos('where', 'what') === false) ;
    if (stripos('where', 'what') !== false) ;

    /* false-positives: weak types magic */
    if (true == strstr('where', 'what')) ;
    if (true != strstr('where', 'what')) ;