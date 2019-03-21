<?php

    if (false !== strpos('where', 'what'))  ;
    if (false === strpos('where', 'what')) ;

    if ('where' || false !== strpos('where', 'what')) ;
    if ('where' && false !== strpos('where', 'what')) ;
    if ('where' or false !== strpos('where', 'what')) ;
    if ('where' and false !== strpos('where', 'what')) ;

    if (false === strpos('where', 'what')) ;
    if (false !== strpos('where', 'what')) ;
    if (false === strpos('where', 'what'))  ;
    if (false !== strpos('where', 'what'))  ;

    if (false === strpos('where', 'what')) ;
    if (false !== strpos('where', 'what')) ;
    if (false === strpos('where', 'what')) ;
    if (false !== strpos('where', 'what')) ;

    if (false === stripos('where', 'what')) ;
    if (false !== stripos('where', 'what')) ;
    if (false === stripos('where', 'what')) ;
    if (false !== stripos('where', 'what')) ;

    /* false-positives: weak types magic */
    if (true == strstr('where', 'what')) ;
    if (true != strstr('where', 'what')) ;