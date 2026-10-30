[English](THIRD-PARTY.md) · [Українська](THIRD-PARTY.uk.md) · [Русский](THIRD-PARTY.ru.md)

# Сторонні компоненти

UltraPermissions: TechsCode. Підготовка цієї збірки з виправленнями безпеки: slyphmp4. Репозиторій зберігає LICENSE; він не замінює ліцензій сторонніх бібліотек/API.

## Вбудовані компоненти виконання

`runtime-libraries` — локальний агрегат наведених нижче компонентів. `compile-api` містить оголошення для компілятора Java; до встановлюваного JAR входить варіант runtime. Класи застосунку компілюються з src/main/java. Хеші бінарних залежностей зафіксовано в config/dependencies.sha256.

| Group:artifact | Версія | Відомості про ліцензію |
|---|---|---|
| com.google.code.gson:gson | 2.13.2 | Apache-2.0 |
| com.google.errorprone:error_prone_annotations | 2.41.0 | Apache 2.0 |
| com.zaxxer:HikariCP | 6.3.3 | The Apache Software License, Version 2.0 |
| commons-codec:commons-codec | 1.19.0 | Див. META-INF/дистрибутив компонента |
| commons-io:commons-io | 2.20.0 | Див. META-INF/дистрибутив компонента |
| commons-logging:commons-logging | 1.2 | Див. META-INF/дистрибутив компонента |
| de.tr7zw:item-nbt-api | 2.15.3 | Див. META-INF/дистрибутив компонента |
| org.apache.commons:commons-lang3 | 3.19.0 | Див. META-INF/дистрибутив компонента |
| org.apache.httpcomponents:httpclient | 4.5.14 | Див. META-INF/дистрибутив компонента |
| org.apache.httpcomponents:httpcore | 4.4.16 | Див. META-INF/дистрибутив компонента |
| org.slf4j:slf4j-api | 2.0.17 | Див. META-INF/дистрибутив компонента |
| org.slf4j:slf4j-nop | 2.0.17 | Див. META-INF/дистрибутив компонента |
| org.bstats:bstats-base | 3.1.0 | Див. META-INF/дистрибутив компонента |
| org.bstats:bstats-bukkit | 3.1.0 | Див. META-INF/дистрибутив компонента |
| org.bstats:bstats-bungeecord | 3.1.0 | Див. META-INF/дистрибутив компонента |
| org.jetbrains:annotations | UNKNOWN | Див. META-INF/дистрибутив компонента |
| net.kyori:adventure-internal-subset | UNKNOWN | Див. META-INF/дистрибутив компонента |

Наявні метадані не визначають точних версій вбудованих JetBrains annotations і внутрішньої підмножини Kyori. Їх позначено як UNKNOWN; не слід приписувати їм довільні версії. bStats 3.1.0 також є переміщеною копією всередині NBT API.

## API, які надає сервер або інтеграції

Файли libs/compile-only використовуються лише для компіляції та тестів і не входять до JAR плагіна. Серед них Spigot/BungeeCord, Vault, PlaceholderAPI, MVdWPlaceholderAPI, LuckPerms, NametagEdit, PermissionsEx, Guava, SnakeYAML, Authlib та annotations. URL і SHA кожного файла наведено в libs/compile-only/provenance.json. Версія в classpath компіляції не визначає версії на робочому сервері.

Ліцензії/NOTICE та метадані Maven, включені до ресурсів META-INF, зберігаються під час збирання. Перевіряйте умови конкретного компонента перед подальшим розповсюдженням. Перевірка хешів у CI підтверджує цілісність, а не відсутність CVE; фактичний склад залежностей сервера/проксі/JDBC перевіряється окремо.
