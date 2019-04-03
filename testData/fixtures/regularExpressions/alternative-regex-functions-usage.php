<?php

    preg_quote('/./');
    preg_quote('/./', '/');

    !<weak_warning descr="'preg_match(...)' would fit more here (also performs better).">preg_match_all</weak_warning>('/./', '...');
    preg_match_all('/./', '...', $matches);
    !preg_match_all('/./', '...', $matches);