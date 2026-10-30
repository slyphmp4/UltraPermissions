<p align="center"><img src="assets/cover.png" alt="UltraPermissions" width="920"></p>

<p align="center">
<a href="https://github.com/slyphmp4/UltraPermissions/actions/workflows/ci.yml"><img src="https://github.com/slyphmp4/UltraPermissions/actions/workflows/ci.yml/badge.svg" alt="Build and verify"></a>
<a href="https://github.com/slyphmp4/UltraPermissions/releases"><img src="https://img.shields.io/github/v/release/slyphmp4/UltraPermissions?include_prereleases" alt="Release"></a>
<img src="https://img.shields.io/badge/build-JDK%2021-268c84" alt="JDK 21">
</p>

# UltraPermissions

Управление правами, группами и рангами Minecraft через команды и графический редактор. **5.6.8-security.1** — community security-сборка с усиленной авторизацией, защищённой синхронизацией и проверками GitHub Actions.

Мы подготовили самостоятельный Gradle-проект, сохранили основные возможности управления правами, переработали опасные сетевые и SQL-пути и добавили отрицательные security-тесты. В этой версии закрыты восемь ранее обнаруженных HIGH/MEDIUM-проблем и дополнительно усилена авторизация административных команд.

Это исправления уязвимостей приложения. Подтверждённых вирусов или намеренного вредоносного импланта в проведённой проверке не установлено, поэтому мы не заявляем «удаление вирусов» или абсолютную безопасность.

## Возможности

- Индивидуальные permissions и группы: разрешения, запреты `-permission.node`, wildcard nodes и наследование.
- Несколько групп у игрока, приоритеты, временная выдача групп и прав, автоматическое истечение сроков.
- Области действия по миру и серверу; отдельные permissions для BungeeCord.
- GUI для пользователей, групп, разрешений и цепочек рангов; иконки и настройки отображения.
- Префиксы, суффиксы, цвета, основной/дополнительный ранг и rank timers.
- Promote/demote по настраиваемым Group Paths.
- Делегированная выдача разрешённых групп с ограничением времени выдачи.
- Локальное JSON-хранилище или MySQL; подписанная синхронизация между доверенными узлами.
- Интеграции Vault, PlaceholderAPI, MVdWPlaceholderAPI и NametagEdit.
- Перенос данных из LuckPerms/PermissionsEx, копирование групп, перенос аккаунтов и File/MySQL transfer.
- Переводы, справочная база permission nodes и журнал проверки прав.

## Скачать и установить

1. Скачайте **UltraPermissions-5.6.8-security.1.jar** из [Releases](https://github.com/slyphmp4/UltraPermissions/releases). `-sources.jar` на сервер не устанавливается.
2. Поместите JAR в `plugins`, убрав другую копию UltraPermissions из той же папки, затем перезапустите сервер.
3. Для начала администрирования выполните **из консоли** `upc AddSuperAdmin ВашНик`, когда пользователь уже зарегистрирован после входа, затем откройте `/uperms`.
4. Для сети или MySQL следуйте [инструкции настройки](docs/CONFIGURATION.md). Все узлы должны использовать эту security-версию.

Платформы проекта — Bukkit/Spigot/Paper и BungeeCord. Собственные классы имеют target Java 17; Java-версия, необходимая конкретному Minecraft-серверу, может быть выше. Сборка и CI используют JDK 21. Объявление api-version 1.13 не является обещанием работоспособности на каждой версии сервера. Полный игровой запуск и реальная TLS-сессия не входят в офлайн CI; первый выпуск помечен pre-release.

## Команды и права

Основные команды: `/uperms`, `/upc`, `/promote`, `/demote`.

- [Полный список команд и примеры](docs/COMMANDS.md)
- [Модель доступа и permission nodes](docs/PERMISSIONS.md)
- [PlaceholderAPI и интеграции](docs/PLACEHOLDERS.md)
- [Изменения безопасности и ограничения](docs/SECURITY-CHANGES.md)

## Собрать в IntelliJ IDEA

Откройте папку как Gradle-проект, используйте **Gradle Wrapper** и **JDK 21** в Gradle JVM.

```powershell
.\gradlew.bat clean build --offline
```

Linux/macOS:

```sh
bash ./gradlew clean build --offline
```

Первый запуск Wrapper скачивает Gradle 8.14.5 с проверкой SHA-256. Зависимости плагина закреплены в `libs`, после загрузки Wrapper сборка не требует внешнего Maven-репозитория. Готовые JAR находятся в `build/libs`.

## CI и проверка компонентов

GitHub Actions запускает чистую сборку на **Ubuntu и Windows / JDK 21**. `verifyComponents` проверяет SHA зависимостей, состав JAR, отсутствие серверных API и тестовых классов в плагине, ресурсы, версию, Java target и **7085 контрактов API**. `verifySecurityFixes` проверяет подделку/повтор сообщений, SQL binding, отказ пула, GUI-сессии, item codecs, TLS policy и административные ветки команд. Сеть и системные процессы в тестовой JVM заблокированы.

Публикация тега `v<version>` запускает обе сборки; релиз создаётся только после их успеха. В assets попадают plugin JAR, sources JAR, полный Gradle-проект и SHA-256. Проверки компонентов не заменяют тестовую установку на точных версиях вашего игрового стека.

## Авторство и лицензии

UltraPermissions — TechsCode; исходные обозначения автора сохранены. Подготовка и сопровождение этой community security-сборки — **slyphmp4**. Лицензия репозитория: [GPL-3.0](LICENSE); сторонние компоненты сохраняют собственные лицензии, см. [THIRD-PARTY.md](THIRD-PARTY.md) и `META-INF` в поставке.
