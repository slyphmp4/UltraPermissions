# Команды

Команды регистрируются программно. Написание подкоманд нечувствительно к регистру. В таблицах `<...>` — подставляемое значение, `[...]` — необязательная часть; скобки вводить не нужно. Автодополнение показывает существующие объекты.

## Основные команды

| Команда | Назначение | Доступ |
|---|---|---|
| `/uperms` | Открыть GUI | Игрок с внутренним superadmin |
| `/uperms <User>` | Открыть редактор пользователя | Игрок с superadmin |
| `/uperms <Group>` | Открыть редактор группы | Игрок с superadmin |
| `/upc` | Справка/дерево административных команд | Console, superadmin или делегированный grant-groups |
| `/promote <User> <GroupPath>` | Продвинуть игрока по цепочке групп | `ultrapermissions.commands.promote` |
| `/demote <User> <GroupPath>` | Понизить игрока по цепочке групп | `ultrapermissions.commands.demote` |

Первого superadmin назначайте **из консоли**: `upc AddSuperAdmin ВашНик`. Сначала игрок должен зайти и получить запись пользователя. Консольные команды вводятся без `/`.

## Все ветки /upc

Список сверен с реальным командным деревом в standalone и сетевом режимах. Формы с `<Server>` появляются при распознанном сервере в настроенной сети; standalone использует варианты без него.

| Синтаксис | Действие | Доступ |
|---|---|---|
| `/upc AddGroup <User> <Group> [1d 2h 5m 10s]` | Добавить игроку группу; допускается время в конце команды. | Делегированный доступ / superadmin / консоль |
| `/upc AddGroupPermission <Group> <Permission> <Server> [options]` | Добавить группе permission, область действия и/или срок. | Superadmin / консоль |
| `/upc AddGroupPermission <Group> <Permission> <Server> <World> [options]` | Добавить группе permission, область действия и/или срок. | Superadmin / консоль |
| `/upc AddPlayerPermission <User> <Permission> <Server> [options]` | Добавить игроку личный permission, область действия и/или срок. | Superadmin / консоль |
| `/upc AddPlayerPermission <User> <Permission> <Server> <World> [options]` | Добавить игроку личный permission, область действия и/или срок. | Superadmin / консоль |
| `/upc AddSuperAdmin <User>` | Выдать внутренний флаг superadmin существующему пользователю. | Superadmin / консоль |
| `/upc copyGroup <Group> <CopyName>` | Скопировать группу под новым именем. | Superadmin / консоль |
| `/upc createGroup <groupName> <Server>` | Создать группу; в сетевом режиме выбирается server/all. | Superadmin / консоль |
| `/upc DeleteGroup <Group>` | Удалить группу. | Superadmin / консоль |
| `/upc deleteUser <User>` | Удалить запись пользователя; пользователь должен быть офлайн. | Superadmin / консоль |
| `/upc listGroupPermissions <Group>` | Показать permissions группы. | Делегированный доступ / superadmin / консоль |
| `/upc listPlayerPermissions <User>` | Показать permissions пользователя. | Делегированный доступ / superadmin / консоль |
| `/upc PurgeSuperAdmins` | Снять superadmin у всех пользователей. | Superadmin / консоль |
| `/upc PurgeGroup <Group>` | Удалить выбранную группу из назначений всех пользователей. | Superadmin / консоль |
| `/upc PurgeGroupPermission <Permission>` | Удалить permission у всех групп. | Superadmin / консоль |
| `/upc PurgeAllPlayerPermission <User>` | Удалить все личные permissions выбранного пользователя. | Superadmin / консоль |
| `/upc PurgePlayerPermission <Permission>` | Удалить permission у всех пользователей. | Superadmin / консоль |
| `/upc PurgePlayerGroups <User>` | Удалить активные группы выбранного пользователя. | Superadmin / консоль |
| `/upc RegisterUser <UUID> <PlayerName> <AssignDefaultGroup>` | Зарегистрировать UUID/имя; true/false управляет назначением default group. | Superadmin / консоль |
| `/upc RemoveGroup <User> <Group>` | Удалить назначение группы у игрока. | Superadmin / консоль |
| `/upc RemoveGroupPermission <Group> <Permission>` | Удалить permission у группы. | Superadmin / консоль |
| `/upc RemovePlayerPermission <User> <Permission>` | Удалить личный permission у игрока. | Superadmin / консоль |
| `/upc RemoveSuperAdmin <User>` | Снять внутренний флаг superadmin. | Superadmin / консоль |
| `/upc RestrictGroupToWorld <Group> <World>` | Ограничить группу миром; all снимает ограничение. | Superadmin / консоль |
| `/upc SetGroups <User> [Group1 Group2 ...]` | Заменить группы пользователя списком после User; пустой список снимает все. | Superadmin / консоль |
| `/upc SetGroupPriority <Group> <Priority>` | Изменить числовой приоритет группы. | Superadmin / консоль |
| `/upc SetGroupPrefix <Group> none` | Изменить префикс группы; none очищает его. | Superadmin / консоль |
| `/upc SetGroupPrefix <Group> <Prefix>` | Изменить префикс группы; none очищает его. | Superadmin / консоль |
| `/upc SetGroupSuffix <Group> none` | Изменить суффикс группы; none очищает его. | Superadmin / консоль |
| `/upc SetGroupSuffix <Group> <Suffix>` | Изменить суффикс группы; none очищает его. | Superadmin / консоль |
| `/upc SetPlayerPrefix clearAll` | Изменить личный префикс; none очищает, clearAll очищает у всех. | Superadmin / консоль |
| `/upc SetPlayerPrefix <User> none` | Изменить личный префикс; none очищает, clearAll очищает у всех. | Superadmin / консоль |
| `/upc SetPlayerPrefix <User> <Prefix>` | Изменить личный префикс; none очищает, clearAll очищает у всех. | Superadmin / консоль |
| `/upc SetPlayerSuffix clearAll` | Изменить личный суффикс; none очищает, clearAll очищает у всех. | Superadmin / консоль |
| `/upc SetPlayerSuffix <User> none` | Изменить личный суффикс; none очищает, clearAll очищает у всех. | Superadmin / консоль |
| `/upc SetPlayerSuffix <User> <Suffix>` | Изменить личный суффикс; none очищает, clearAll очищает у всех. | Superadmin / консоль |
| `/upc TransferAccount <Groups / Permissions / All> <Overwrite / Append> <User> <User>` | Перенести Groups/Permissions/All с первого User на второго; Overwrite/Append задаёт замену/добавление. | Superadmin / консоль |
| `/upc Transfer <File / MySQL> <File / MySQL>` | Перенести хранилище File↔MySQL; требуется рабочая MySQL. | Superadmin / консоль |
| `/upc AddGroupPermission <Group> <Permission> [options]` | Добавить группе permission, область действия и/или срок. | Superadmin / консоль |
| `/upc AddGroupPermission <Group> <Permission> <World> [options]` | Добавить группе permission, область действия и/или срок. | Superadmin / консоль |
| `/upc AddPlayerPermission <User> <Permission> [options]` | Добавить игроку личный permission, область действия и/или срок. | Superadmin / консоль |
| `/upc AddPlayerPermission <User> <Permission> <World> [options]` | Добавить игроку личный permission, область действия и/или срок. | Superadmin / консоль |
| `/upc createGroup <groupName>` | Создать группу; в сетевом режиме выбирается server/all. | Superadmin / консоль |

## Значения и опции

- `User` — сохранённое имя пользователя, `Group` — имя существующей группы; `CopyName`/`groupName` задаются одним аргументом.
- `Server` — `all`, `local` или имя сервера из списка прокси. `World` — существующий мир или `all`.
- `Permission` — permission node; начальный `-` задаёт запрет, например `-minecraft.command.op`.
- `[options]` для добавления permissions поддерживает срок `1d 2h 5m 10s`, имя мира и контекстные слова `local`/`bungee`; явные Server/World в сетевой форме задают соответствующую область. Без срока назначение постоянное.
- `AddGroup` принимает те же сокращения времени в конце команды; делегирование дополнительно ограничивает группу и максимальный срок выдачи.
- `SetGroups` принимает остальные аргументы как имена групп, неизвестные имена пропускает. **Сначала снимаются прежние группы**; используйте точные имена.
- `Prefix`/`Suffix` поддерживают форматирование; для удаления есть `none`, а `clearAll` у пользовательских форм действует на всех пользователей.

## Примеры

Standalone, из консоли:

```text
upc createGroup vip
upc AddGroupPermission vip essentials.fly
upc AddPlayerPermission Steve -minecraft.command.op
upc AddGroup Steve vip 7d
upc AddSuperAdmin Steve
upc SetGroupPrefix vip &a[VIP]
upc SetGroups Steve default vip
```

В сетевом режиме при добавлении прав укажите server, например:

```text
upc AddGroupPermission vip essentials.fly all
upc AddPlayerPermission Steve essentials.home survival
```

`Purge*`, `Delete*`, `SetGroups`, Transfer и Overwrite меняют данные массово. Используйте копию хранилища перед административными переносами; GUI не заменяет этот смысл команд.
