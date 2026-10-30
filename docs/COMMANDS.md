[English](COMMANDS.md) · [Українська](COMMANDS.uk.md) · [Русский](COMMANDS.ru.md)

# Commands

Commands are registered programmatically. Subcommand names are case-insensitive. In the tables, `<...>` is a value to supply and `[...]` is optional; do not type the brackets. Tab completion suggests existing objects.

## Main commands

| Command | Purpose | Access |
|---|---|---|
| `/uperms` | Open the GUI | Player with the internal superadmin flag |
| `/uperms <User>` | Open the user editor | Player with superadmin |
| `/uperms <Group>` | Open the group editor | Player with superadmin |
| `/upc` | Help/administrative command tree | Console, superadmin or delegated grant-groups access |
| `/promote <User> <GroupPath>` | Move a player up a group path | `ultrapermissions.commands.promote` |
| `/demote <User> <GroupPath>` | Move a player down a group path | `ultrapermissions.commands.demote` |

Assign the first superadmin **from the console**: `upc AddSuperAdmin YourName`. The player must first join and have a user record. Console commands are entered without `/`.

## All /upc branches

This list is checked against the actual command tree in standalone and network modes. Forms with `<Server>` appear when the server is recognized in a configured network; standalone uses the forms without it.

| Syntax | Action | Access |
|---|---|---|
| `/upc AddGroup <User> <Group> [1d 2h 5m 10s]` | Assign a group to a player; a duration may follow the command. | Delegated access / superadmin / console |
| `/upc AddGroupPermission <Group> <Permission> <Server> [options]` | Add a permission to a group, with scope and/or expiration. | Superadmin / console |
| `/upc AddGroupPermission <Group> <Permission> <Server> <World> [options]` | Add a permission to a group, with scope and/or expiration. | Superadmin / console |
| `/upc AddPlayerPermission <User> <Permission> <Server> [options]` | Add a personal permission to a player, with scope and/or expiration. | Superadmin / console |
| `/upc AddPlayerPermission <User> <Permission> <Server> <World> [options]` | Add a personal permission to a player, with scope and/or expiration. | Superadmin / console |
| `/upc AddSuperAdmin <User>` | Grant the internal superadmin flag to an existing user. | Superadmin / console |
| `/upc copyGroup <Group> <CopyName>` | Copy a group under a new name. | Superadmin / console |
| `/upc createGroup <groupName> <Server>` | Create a group; network mode selects a server or all. | Superadmin / console |
| `/upc DeleteGroup <Group>` | Delete a group. | Superadmin / console |
| `/upc deleteUser <User>` | Delete a user record; the user must be offline. | Superadmin / console |
| `/upc listGroupPermissions <Group>` | List a group's permissions. | Delegated access / superadmin / console |
| `/upc listPlayerPermissions <User>` | List a user's permissions. | Delegated access / superadmin / console |
| `/upc PurgeSuperAdmins` | Remove superadmin from every user. | Superadmin / console |
| `/upc PurgeGroup <Group>` | Remove the selected group assignment from every user. | Superadmin / console |
| `/upc PurgeGroupPermission <Permission>` | Remove a permission from every group. | Superadmin / console |
| `/upc PurgeAllPlayerPermission <User>` | Remove all personal permissions from the selected user. | Superadmin / console |
| `/upc PurgePlayerPermission <Permission>` | Remove a permission from every user. | Superadmin / console |
| `/upc PurgePlayerGroups <User>` | Remove the selected user's active groups. | Superadmin / console |
| `/upc RegisterUser <UUID> <PlayerName> <AssignDefaultGroup>` | Register a UUID/name; true/false controls default-group assignment. | Superadmin / console |
| `/upc RemoveGroup <User> <Group>` | Remove a player's group assignment. | Superadmin / console |
| `/upc RemoveGroupPermission <Group> <Permission>` | Remove a permission from a group. | Superadmin / console |
| `/upc RemovePlayerPermission <User> <Permission>` | Remove a player's personal permission. | Superadmin / console |
| `/upc RemoveSuperAdmin <User>` | Remove the internal superadmin flag. | Superadmin / console |
| `/upc RestrictGroupToWorld <Group> <World>` | Restrict a group to a world; all removes the restriction. | Superadmin / console |
| `/upc SetGroups <User> [Group1 Group2 ...]` | Replace the user's groups with the list after User; an empty list removes all. | Superadmin / console |
| `/upc SetGroupPriority <Group> <Priority>` | Change the group's numeric priority. | Superadmin / console |
| `/upc SetGroupPrefix <Group> none` | Change the group prefix; none clears it. | Superadmin / console |
| `/upc SetGroupPrefix <Group> <Prefix>` | Change the group prefix; none clears it. | Superadmin / console |
| `/upc SetGroupSuffix <Group> none` | Change the group suffix; none clears it. | Superadmin / console |
| `/upc SetGroupSuffix <Group> <Suffix>` | Change the group suffix; none clears it. | Superadmin / console |
| `/upc SetPlayerPrefix clearAll` | Change a personal prefix; none clears it, clearAll clears it for everyone. | Superadmin / console |
| `/upc SetPlayerPrefix <User> none` | Change a personal prefix; none clears it, clearAll clears it for everyone. | Superadmin / console |
| `/upc SetPlayerPrefix <User> <Prefix>` | Change a personal prefix; none clears it, clearAll clears it for everyone. | Superadmin / console |
| `/upc SetPlayerSuffix clearAll` | Change a personal suffix; none clears it, clearAll clears it for everyone. | Superadmin / console |
| `/upc SetPlayerSuffix <User> none` | Change a personal suffix; none clears it, clearAll clears it for everyone. | Superadmin / console |
| `/upc SetPlayerSuffix <User> <Suffix>` | Change a personal suffix; none clears it, clearAll clears it for everyone. | Superadmin / console |
| `/upc TransferAccount <Groups / Permissions / All> <Overwrite / Append> <User> <User>` | Transfer Groups/Permissions/All from the first User to the second; Overwrite/Append selects replacement/appending. | Superadmin / console |
| `/upc Transfer <File / MySQL> <File / MySQL>` | Transfer storage between File and MySQL; working MySQL is required. | Superadmin / console |
| `/upc AddGroupPermission <Group> <Permission> [options]` | Add a permission to a group, with scope and/or expiration. | Superadmin / console |
| `/upc AddGroupPermission <Group> <Permission> <World> [options]` | Add a permission to a group, with scope and/or expiration. | Superadmin / console |
| `/upc AddPlayerPermission <User> <Permission> [options]` | Add a personal permission to a player, with scope and/or expiration. | Superadmin / console |
| `/upc AddPlayerPermission <User> <Permission> <World> [options]` | Add a personal permission to a player, with scope and/or expiration. | Superadmin / console |
| `/upc createGroup <groupName>` | Create a group; network mode selects a server or all. | Superadmin / console |

## Values and options

- `User` is a saved player name; `Group` is an existing group name. Supply `CopyName`/`groupName` as a single argument.
- `Server` is `all`, `local` or a server name from the proxy list. `World` is an existing world or `all`.
- `Permission` is a permission node; a leading `-` specifies denial, for example `-minecraft.command.op`.
- `[options]` for permission assignments supports a duration such as `1d 2h 5m 10s`, a world name and the context words `local`/`bungee`. Explicit Server/World arguments in network forms select the corresponding scope. Assignments without a duration are permanent.
- `AddGroup` accepts the same time abbreviations at the end of the command. Delegation additionally limits the allowed group and maximum assignment duration.
- `SetGroups` treats the remaining arguments as group names and skips unknown names. **Existing groups are removed first**; use exact names.
- `Prefix`/`Suffix` support formatting. Use `none` to remove a value; `clearAll` in user forms affects every user.

## Examples

Standalone, from the console:

```text
upc createGroup vip
upc AddGroupPermission vip essentials.fly
upc AddPlayerPermission Steve -minecraft.command.op
upc AddGroup Steve vip 7d
upc AddSuperAdmin Steve
upc SetGroupPrefix vip &a[VIP]
upc SetGroups Steve default vip
```

In network mode, specify a server when adding permissions, for example:

```text
upc AddGroupPermission vip essentials.fly all
upc AddPlayerPermission Steve essentials.home survival
```

`Purge*`, `Delete*`, `SetGroups`, Transfer and Overwrite can change data in bulk. Keep a storage copy before administrative transfers; using a GUI does not change the effect of these operations.
