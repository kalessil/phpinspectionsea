# Language level migration

## Alias functions usage

In PHP, [some functions](http://php.net/manual/en/aliases.php) don't have their own implementation. They simply reference 
other functions. These are called "alias functions". Alias functions are maintained for backward compatibility.

In order to avoid surprises (e.g. mysqli_* aliases were dropped in PHP 5.4 or magic_quotes_runtime was dropped in PHP 7.0),
we encourage to not rely on alias functions at all.
