[English](COMMANDS.md) · [Українська](COMMANDS.uk.md) · [Русский](COMMANDS.ru.md)

# Команди

Команди реєструються програмно. Назви підкоманд нечутливі до регістру. У таблицях `<...>` — значення, яке потрібно підставити, `[...]` — необов'язкова частина; дужки вводити не потрібно. Автодоповнення пропонує наявні об'єкти.

## Основні команди

| Команда | Призначення | Доступ |
|---|---|---|
| `/uperms` | Відкрити графічний редактор | Гравець із внутрішнім прапорцем superadmin |
| `/uperms <User>` | Відкрити редактор користувача | Гравець із superadmin |
| `/uperms <Group>` | Відкрити редактор групи | Гравець із superadmin |
| `/upc` | Довідка/дерево адміністративних команд | Консоль, superadmin або делегований доступ grant-groups |
| `/promote <User> <GroupPath>` | Підвищити гравця за ланцюжком груп | `ultrapermissions.commands.promote` |
| `/demote <User> <GroupPath>` | Понизити гравця за ланцюжком груп | `ultrapermissions.commands.demote` |

Призначайте першого superadmin **з консолі**: `upc AddSuperAdmin ВашНік`. Спочатку гравець має приєднатися й отримати запис користувача. Консольні команди вводяться без `/`.

## Усі гілки /upc

Список звірено з реальним деревом команд в окремому та мережевому режимах. Форми з `<Server>` з'являються, коли сервер розпізнано в налаштованій мережі; окремий сервер використовує варіанти без нього.

| Синтаксис | Дія | Доступ |
|---|---|---|
| `/upc AddGroup <User> <Group> [1d 2h 5m 10s]` | Надати гравцю групу; наприкінці команди можна вказати строк. | Делегований доступ / superadmin / консоль |
| `/upc AddGroupPermission <Group> <Permission> <Server> [options]` | Додати групі право, область дії та/або строк. | Superadmin / консоль |
| `/upc AddGroupPermission <Group> <Permission> <Server> <World> [options]` | Додати групі право, область дії та/або строк. | Superadmin / консоль |
| `/upc AddPlayerPermission <User> <Permission> <Server> [options]` | Додати гравцю особисте право, область дії та/або строк. | Superadmin / консоль |
| `/upc AddPlayerPermission <User> <Permission> <Server> <World> [options]` | Додати гравцю особисте право, область дії та/або строк. | Superadmin / консоль |
| `/upc AddSuperAdmin <User>` | Надати внутрішній прапорець superadmin наявному користувачу. | Superadmin / консоль |
| `/upc copyGroup <Group> <CopyName>` | Скопіювати групу під новим іменем. | Superadmin / консоль |
| `/upc createGroup <groupName> <Server>` | Створити групу; у мережевому режимі вибирається сервер або all. | Superadmin / консоль |
| `/upc DeleteGroup <Group>` | Видалити групу. | Superadmin / консоль |
| `/upc deleteUser <User>` | Видалити запис користувача; користувач має бути офлайн. | Superadmin / консоль |
| `/upc listGroupPermissions <Group>` | Показати права групи. | Делегований доступ / superadmin / консоль |
| `/upc listPlayerPermissions <User>` | Показати права користувача. | Делегований доступ / superadmin / консоль |
| `/upc PurgeSuperAdmins` | Зняти superadmin з усіх користувачів. | Superadmin / консоль |
| `/upc PurgeGroup <Group>` | Видалити призначення вибраної групи в усіх користувачів. | Superadmin / консоль |
| `/upc PurgeGroupPermission <Permission>` | Видалити право в усіх груп. | Superadmin / консоль |
| `/upc PurgeAllPlayerPermission <User>` | Видалити всі особисті права вибраного користувача. | Superadmin / консоль |
| `/upc PurgePlayerPermission <Permission>` | Видалити право в усіх користувачів. | Superadmin / консоль |
| `/upc PurgePlayerGroups <User>` | Видалити активні групи вибраного користувача. | Superadmin / консоль |
| `/upc RegisterUser <UUID> <PlayerName> <AssignDefaultGroup>` | Зареєструвати UUID/ім'я; true/false керує призначенням групи за замовчуванням. | Superadmin / консоль |
| `/upc RemoveGroup <User> <Group>` | Видалити призначення групи в гравця. | Superadmin / консоль |
| `/upc RemoveGroupPermission <Group> <Permission>` | Видалити право в групи. | Superadmin / консоль |
| `/upc RemovePlayerPermission <User> <Permission>` | Видалити особисте право в гравця. | Superadmin / консоль |
| `/upc RemoveSuperAdmin <User>` | Зняти внутрішній прапорець superadmin. | Superadmin / консоль |
| `/upc RestrictGroupToWorld <Group> <World>` | Обмежити групу світом; all знімає обмеження. | Superadmin / консоль |
| `/upc SetGroups <User> [Group1 Group2 ...]` | Замінити групи користувача списком після User; порожній список знімає всі. | Superadmin / консоль |
| `/upc SetGroupPriority <Group> <Priority>` | Змінити числовий пріоритет групи. | Superadmin / консоль |
| `/upc SetGroupPrefix <Group> none` | Змінити префікс групи; none очищує його. | Superadmin / консоль |
| `/upc SetGroupPrefix <Group> <Prefix>` | Змінити префікс групи; none очищує його. | Superadmin / консоль |
| `/upc SetGroupSuffix <Group> none` | Змінити суфікс групи; none очищує його. | Superadmin / консоль |
| `/upc SetGroupSuffix <Group> <Suffix>` | Змінити суфікс групи; none очищує його. | Superadmin / консоль |
| `/upc SetPlayerPrefix clearAll` | Змінити особистий префікс; none очищує його, clearAll очищує в усіх. | Superadmin / консоль |
| `/upc SetPlayerPrefix <User> none` | Змінити особистий префікс; none очищує його, clearAll очищує в усіх. | Superadmin / консоль |
| `/upc SetPlayerPrefix <User> <Prefix>` | Змінити особистий префікс; none очищує його, clearAll очищує в усіх. | Superadmin / консоль |
| `/upc SetPlayerSuffix clearAll` | Змінити особистий суфікс; none очищує його, clearAll очищує в усіх. | Superadmin / консоль |
| `/upc SetPlayerSuffix <User> none` | Змінити особистий суфікс; none очищує його, clearAll очищує в усіх. | Superadmin / консоль |
| `/upc SetPlayerSuffix <User> <Suffix>` | Змінити особистий суфікс; none очищує його, clearAll очищує в усіх. | Superadmin / консоль |
| `/upc TransferAccount <Groups / Permissions / All> <Overwrite / Append> <User> <User>` | Перенести Groups/Permissions/All з першого User на другого; Overwrite/Append задає заміну/додавання. | Superadmin / консоль |
| `/upc Transfer <File / MySQL> <File / MySQL>` | Перенести сховище між File та MySQL; потрібна працездатна MySQL. | Superadmin / консоль |
| `/upc AddGroupPermission <Group> <Permission> [options]` | Додати групі право, область дії та/або строк. | Superadmin / консоль |
| `/upc AddGroupPermission <Group> <Permission> <World> [options]` | Додати групі право, область дії та/або строк. | Superadmin / консоль |
| `/upc AddPlayerPermission <User> <Permission> [options]` | Додати гравцю особисте право, область дії та/або строк. | Superadmin / консоль |
| `/upc AddPlayerPermission <User> <Permission> <World> [options]` | Додати гравцю особисте право, область дії та/або строк. | Superadmin / консоль |
| `/upc createGroup <groupName>` | Створити групу; у мережевому режимі вибирається сервер або all. | Superadmin / консоль |

## Значення та параметри

- `User` — збережене ім'я гравця, `Group` — ім'я наявної групи; `CopyName`/`groupName` задаються одним аргументом.
- `Server` — `all`, `local` або ім'я сервера зі списку проксі. `World` — наявний світ або `all`.
- `Permission` — вузол прав; початковий `-` задає заборону, наприклад `-minecraft.command.op`.
- `[options]` для надання прав підтримує строк `1d 2h 5m 10s`, ім'я світу та контекстні слова `local`/`bungee`. Явні Server/World у мережевій формі задають відповідну область дії. Без строку призначення постійне.
- `AddGroup` приймає ті самі скорочення часу наприкінці команди. Делегування додатково обмежує дозволену групу та максимальний строк надання.
- `SetGroups` сприймає решту аргументів як імена груп, невідомі імена пропускає. **Спочатку знімаються попередні групи**; використовуйте точні імена.
- `Prefix`/`Suffix` підтримують форматування. Для видалення є `none`, а `clearAll` у користувацьких формах діє на всіх користувачів.

## Приклади

Окремий сервер, з консолі:

```text
upc createGroup vip
upc AddGroupPermission vip essentials.fly
upc AddPlayerPermission Steve -minecraft.command.op
upc AddGroup Steve vip 7d
upc AddSuperAdmin Steve
upc SetGroupPrefix vip &a[VIP]
upc SetGroups Steve default vip
```

У мережевому режимі під час додавання прав зазначте сервер, наприклад:

```text
upc AddGroupPermission vip essentials.fly all
upc AddPlayerPermission Steve essentials.home survival
```

`Purge*`, `Delete*`, `SetGroups`, Transfer та Overwrite можуть масово змінювати дані. Зберігайте копію сховища перед адміністративними перенесеннями; графічний редактор не змінює наслідків цих операцій.
