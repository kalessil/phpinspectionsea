<?php
    /* @var array $files */
    $class = \DateTime::class;

    foreach ($files as $index => & $file1) {
        <weak_warning descr="Objects should be created outside of a loop and cloned instead">$now = new $class();</weak_warning>
        <weak_warning descr="Objects should be created outside of a loop and cloned instead">$domElement = (new \DOMDocument())->createElement('');</weak_warning>

        <weak_warning descr="This statement seems to be disconnected from parent foreach">error_log('Processing next file', 3, '/tmp/debug');</weak_warning>
        <weak_warning descr="This statement seems to be disconnected from parent foreach">error_log($class, 3, '/tmp/debug');</weak_warning>

        <weak_warning descr="This statement seems to be disconnected from parent foreach">for</weak_warning> (; $for < 10;){
            echo $for;
        }
        <weak_warning descr="This statement seems to be disconnected from parent foreach">foreach</weak_warning> ([] as $foreach){
            echo $foreach;
        }
        <weak_warning descr="This statement seems to be disconnected from parent foreach">while</weak_warning> ($while < 10) {
            echo $while;
        }

        <weak_warning descr="This statement seems to be disconnected from parent foreach">if</weak_warning> ($if > 0) {
            echo $if;
        }

        <weak_warning descr="This statement seems to be disconnected from parent foreach">switch</weak_warning> ($switch) {
            case 0:
            case 1:
            default:
                break;
        }

        <weak_warning descr="This statement seems to be disconnected from parent foreach">try</weak_warning> {
            throw new \RuntimeException('');
        } catch (\Exception $e) {
            echo $e->getMessage();
        }
    }
?>

<?php foreach ([] as $item): ?>
    <a href=""><?= <error descr="Expected: expression">?</error>></a>
<?php endforeach; ?>
