<?php

if (<weak_warning descr="This negative if conditional could be avoided">!expr()</weak_warning>)
{ a(); b(); }
else
{ c(); d(); }

if ( <weak_warning descr="This negative if conditional could be avoided">! expr()</weak_warning> )
{ a(); b(); }
else
{ c(); d(); }

if (!true)
{ a(); b(); }
elseif (<weak_warning descr="This negative if conditional could be avoided">!false</weak_warning>)
{ c(); d(); }
else
{ e(); f(); }

if (!true)
{ a(); b(); }
else if (<weak_warning descr="This negative if conditional could be avoided">!false</weak_warning>)
{ c(); d(); }
else
{ e(); f(); }

if (<weak_warning descr="This negative if conditional could be avoided">!(true === true)</weak_warning>)
{ a(); b(); }
else
{ c(); d(); }

// Complex cases.
if (<weak_warning descr="This negative if conditional could be avoided">!true</weak_warning>){a();b();}else{c();d();}

if (<weak_warning descr="This negative if conditional could be avoided">!true</weak_warning>){a();b();}else c();

if (<weak_warning descr="This negative if conditional could be avoided">!true</weak_warning>)a();else{b();c();}

if (<weak_warning descr="This negative if conditional could be avoided">!true</weak_warning>) a();else{b();c();}

if (<weak_warning descr="This negative if conditional could be avoided">!true</weak_warning>)a();else b();

// False-positives.
if (!true)
{ }

if (!true && !false)
{ }
else
{ }
