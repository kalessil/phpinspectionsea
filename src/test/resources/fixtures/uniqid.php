<?php
    <error descr="Please provide both prefix and more entropy parameters (see CVE-2010-1128)">uniqid()</error>;
    <error descr="Please provide both prefix and more entropy parameters (see CVE-2010-1128)">uniqid('q')</error>;

    uniqid('', true);