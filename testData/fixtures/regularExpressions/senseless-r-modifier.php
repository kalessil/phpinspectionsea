<?php

    preg_match('/abc./iur', '');

    preg_match(<error descr="[EA] /u modifier is missing (r modifier found).">'/abc./ir'</error>, '');
    preg_match(<error descr="[EA] /i modifier is missing (r modifier found).">'/abc./ur'</error>, '');
