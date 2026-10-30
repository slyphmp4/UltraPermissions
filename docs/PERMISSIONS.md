# Права и модель доступа

## Внутренний superadmin

Superadmin — **флаг записи User в UltraPermissions**, а не permission node `ultrapermissions.admin`. Такого node для GUI здесь нет. Сам по себе OP или `*` не заменяет этот флаг при открытии `/uperms` и проверке административных веток `/upc`.

- Выдать: консоль `upc AddSuperAdmin <User>`.
- Снять: `upc RemoveSuperAdmin <User>`.
- Снять у всех: `upc PurgeSuperAdmins`.

GUI требует игрока с текущим флагом; консоль работает через `/upc`. При отзыве роли существующая GUI/chat-сессия больше не может менять permissions. Административные ветки команд также проверяют роль заново.

## Реальные permission nodes

| Node | Назначение |
|---|---|
| `ultrapermissions.commands.promote` | Доступ к `/promote <User> <GroupPath>` |
| `ultrapermissions.commands.demote` | Доступ к `/demote <User> <GroupPath>` |

У остальных административных операций используется внутренний superadmin, а не набор вымышленных `ultrapermissions.*` permission nodes. Эти две команды проходят обычную проверку Bukkit permissions; OP/default rules зависят от конфигурации сервера и плагина.

## Делегирование групп

В GUI группы можно настроить разрешённые к выдаче группы и максимальный срок выдачи (grant groups). Пользователь с такой действующей группой получает ограниченный доступ к `/upc`:

- `AddGroup` — только разрешённые группы и допустимый срок.
- `listGroupPermissions`, `listPlayerPermissions` — просмотр списков.

Делегирование не даёт редактор GUI, создание пользователей, изменение суффиксов или прочие административные операции. Они требуют superadmin. Неизвестному пользователю доступ закрывается без исключения.

## Вычисление прав

Permissions назначаются пользователям и группам, могут наследоваться и ограничиваться миром/сервером/временем. `example.node` — разрешение, `-example.node` — запрет. Wildcards, приоритет групп, персональные permissions, Bukkit defaults/OP и настройка negative override участвуют в lookup; при конфликте проверяйте результат в permission logger, не делайте вывод только по одному wildcard.

Консоль и доверенные плагины с доступом к API являются административной границей. Общий HMAC-key кластера означает доверие всем его держателям. MySQL-аккаунт и файлы Registry/mysql.properties/security.properties не должны быть доступны игрокам.
