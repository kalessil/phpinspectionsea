<?php

    mkdir('test'); // <-- reported

    if (!is_dir('test')) {
        mkdir('test'); // <-- reported
    }

    if ((!mkdir('test'))) { // <- if reported
       echo 'not created';
    }

    if (!is_dir('test') && !mkdir('test')) { // <- mkdir reported
        echo 'not created';
    }