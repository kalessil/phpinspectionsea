<?php

    if (strncmp('first', 'second', 5) === 0) {     // <- reported
        return;
    }
    if (strncasecmp('first', 'second', 5) === 0) { // <- reported
        return;
    }

    if (!strncmp('first', 'second', 5)) {          // <- reported
        return;
    }
    if (!strncasecmp('first', 'second', 5)) {      // <- reported
        return;
    }
