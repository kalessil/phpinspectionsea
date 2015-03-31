<?php

    if (array_search('', array(), true))           {}
    if (array_search('', array(), true) || false)  {}
    if (array_search('', array(), true) && true)   {}

    if (array_search('', array(), true) === false) {}
    if (array_search('', array(), true) !== false) {}