[English](THIRD-PARTY.md) · [Українська](THIRD-PARTY.uk.md) · [Русский](THIRD-PARTY.ru.md)

# Сторонние компоненты

UltraPermissions: TechsCode. Подготовка этой сборки с исправлениями безопасности: slyphmp4. Репозиторий сохраняет LICENSE; он не заменяет лицензии сторонних библиотек/API.

## Встроенные компоненты выполнения

`runtime-libraries` — локальный агрегат приведённых компонентов. `compile-api` содержит декларации для Java-компилятора; в устанавливаемый JAR попадает runtime-вариант. Классы приложения компилируются из src/main/java. SHA бинарных зависимостей закреплены в config/dependencies.sha256.

| Group:artifact | Версия | Сведения о лицензии |
|---|---|---|
| com.google.code.gson:gson | 2.15.0 | Apache-2.0 |
| com.google.errorprone:error_prone_annotations | 2.52.0 | Apache 2.0 |
| com.zaxxer:HikariCP | 7.1.0 | The Apache Software License, Version 2.0 |
| commons-codec:commons-codec | 1.23.0 | См. META-INF/поставку компонента |
| commons-io:commons-io | 2.22.0 | См. META-INF/поставку компонента |
| commons-logging:commons-logging | 1.4.0 | См. META-INF/поставку компонента |
| de.tr7zw:item-nbt-api | 2.16.1 | См. META-INF/поставку компонента |
| org.apache.commons:commons-lang3 | 3.21.0 | См. META-INF/поставку компонента |
| org.apache.httpcomponents:httpclient | 4.5.14 | См. META-INF/поставку компонента |
| org.apache.httpcomponents:httpcore | 4.4.16 | См. META-INF/поставку компонента |
| org.slf4j:slf4j-api | 2.0.20 | См. META-INF/поставку компонента |
| org.slf4j:slf4j-nop | 2.0.20 | См. META-INF/поставку компонента |
| org.bstats:bstats-base | 3.2.1 | См. META-INF/поставку компонента |
| org.bstats:bstats-bukkit | 3.2.1 | См. META-INF/поставку компонента |
| org.bstats:bstats-bungeecord | 3.2.1 | См. META-INF/поставку компонента |
| org.jetbrains:annotations | UNKNOWN | См. META-INF/поставку компонента |
| net.kyori:adventure-internal-subset | UNKNOWN | См. META-INF/поставку компонента |

Точные версии встроенных JetBrains annotations и внутренней подгруппы Kyori в доступных метаданных не определены. Это отмечено как UNKNOWN; не следует приписывать им произвольные версии. bStats 3.2.1 также присутствует как перемещённая копия в NBT API.

## API, предоставляемые сервером/интеграциями

Файлы libs/compile-only используются только для компиляции и тестов, в JAR плагина не включаются. Там находятся Spigot/BungeeCord, Vault, PlaceholderAPI, MVdWPlaceholderAPI, LuckPerms, NametagEdit, PermissionsEx, Guava, SnakeYAML, Authlib и annotations. URL и SHA каждого файла — libs/compile-only/provenance.json. Наличие версии в пути классов компиляции не определяет версию живого сервера.

Лицензии/NOTICE и метаданные Maven, включённые в META-INF ресурсов, сохраняются при сборке. Проверяйте условия конкретного компонента при дальнейшем распространении. SHA-контроль CI проверяет целостность, а не отсутствие CVE; фактический состав зависимостей сервера/прокси/JDBC проверяется отдельно.

Обновлённый runtime и его объявления для компилятора совпадают и закреплены в config/runtime-components.json. tools/refresh_runtime_bundle.py используется явно при сопровождении; обычная сборка остаётся офлайн. Apache HTTP 4.x и подмножества Kyori/JetBrains с неизвестными версиями побайтно сохранены из закреплённой исходной поставки.
