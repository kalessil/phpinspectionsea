<?php

    <warning descr="[EA] Second parameter should be provided (for proper symbols escaping).">preg_quote('/./')</warning>;
    preg_quote('/./', '/');

    !<weak_warning descr="[EA] 'preg_match(...)' would fit more here (also performs better).">preg_match_all</weak_warning>('/./', '...');
    preg_match_all('/./', '...', $matches);
    !preg_match_all('/./', '...', $matches);