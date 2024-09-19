<?php

function returnsBool(): bool { return true; }
function returnsNullableBool(): ?bool { return true; }

if (!returnsBool())
{ a(); b(); }
<weak_warning descr="[EA] The if-else workflow is driven by inverted conditions, consider avoiding invertions.">else</weak_warning>
{ c(); d(); }

if ( ! returnsBool() )
{ a(); b(); }
<weak_warning descr="[EA] The if-else workflow is driven by inverted conditions, consider avoiding invertions.">else</weak_warning>
{ c(); d(); }

if (!true)
{ a(); b(); }
elseif (!false)
{ c(); d(); }
<weak_warning descr="[EA] The if-else workflow is driven by inverted conditions, consider avoiding invertions.">else</weak_warning>
{ e(); f(); }

if (!true)
{ a(); b(); }
else if (!false)
{ c(); d(); }
<weak_warning descr="[EA] The if-else workflow is driven by inverted conditions, consider avoiding invertions.">else</weak_warning>
{ e(); f(); }

if (!(true === true))
{ a(); b(); }
<weak_warning descr="[EA] The if-else workflow is driven by inverted conditions, consider avoiding invertions.">else</weak_warning>
{ c(); d(); }

if (false === returnsBool())
{ a(); b(); }
<weak_warning descr="[EA] The if-else workflow is driven by inverted conditions, consider avoiding invertions.">else</weak_warning>
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

if (!empty([])) {}
else {}

if (false === returnsNullableBool())
{ a(); b(); }
else
{ c(); d(); }