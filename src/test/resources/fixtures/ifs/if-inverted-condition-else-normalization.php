<?php

if (!expr())
{ a(); b(); }
<weak_warning descr="The if-else workflow is driven by inverted conditions, consider avoiding invertions.">else</weak_warning>
{ c(); d(); }

if ( ! expr() )
{ a(); b(); }
<weak_warning descr="The if-else workflow is driven by inverted conditions, consider avoiding invertions.">else</weak_warning>
{ c(); d(); }

if (!true)
{ a(); b(); }
elseif (!false)
{ c(); d(); }
<weak_warning descr="The if-else workflow is driven by inverted conditions, consider avoiding invertions.">else</weak_warning>
{ e(); f(); }

if (!true)
{ a(); b(); }
else if (!false)
{ c(); d(); }
<weak_warning descr="The if-else workflow is driven by inverted conditions, consider avoiding invertions.">else</weak_warning>
{ e(); f(); }

if (!(true === true))
{ a(); b(); }
<weak_warning descr="The if-else workflow is driven by inverted conditions, consider avoiding invertions.">else</weak_warning>
{ c(); d(); }

if (false === expr())
{ a(); b(); }
<weak_warning descr="The if-else workflow is driven by inverted conditions, consider avoiding invertions.">else</weak_warning>
{ c(); d(); }

/* not supported: must follow PSR and use `{}` */
if (!true){a();b();}else c();
if (!true)a();else{b();c();}
if (!true) a();else{b();c();}
if (!true)a();else b();

/* false-positives */
if (!true) {}

if (!true && !false) {}
else {}
