<?php

    $content = join('', file('...'));     // <- reported

    $content = implode('', file('...'));  // <- reported
    $content = @implode('', file('...')); // <- reported
    $content = implode('', @file('...')); // <- reported

    $a = '';
    $content = implode($a, file('...')); // <- reported

    $content = implode('-', file('...'));
    $content = implode('-', file('...', FILE_IGNORE_NEW_LINES));
    $content = implode('-', file('...', FILE_USE_INCLUDE_PATH, null));