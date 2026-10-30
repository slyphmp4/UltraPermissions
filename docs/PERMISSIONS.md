[English](PERMISSIONS.md) · [Українська](PERMISSIONS.uk.md) · [Русский](PERMISSIONS.ru.md)

# Permissions and access model

## Internal superadmin

Superadmin is **a flag on an UltraPermissions User record**. The GUI has no `ultrapermissions.admin` permission node. OP status or `*` alone does not replace this flag when opening `/uperms` or checking administrative `/upc` branches.

- Grant it: console `upc AddSuperAdmin <User>`.
- Revoke it: `upc RemoveSuperAdmin <User>`.
- Revoke it from everyone: `upc PurgeSuperAdmins`.

The GUI requires a player with the current flag; console administration uses `/upc`. Once the role is revoked, an existing GUI/chat session can no longer change permissions. Administrative command branches also check the role again.

## Available permission nodes

| Node | Purpose |
|---|---|
| `ultrapermissions.commands.promote` | Access to `/promote <User> <GroupPath>` |
| `ultrapermissions.commands.demote` | Access to `/demote <User> <GroupPath>` |

Other administrative operations use the internal superadmin flag. These two commands use ordinary Bukkit permission checks; OP/default rules depend on the server and plugin configuration. Do not assume that additional `ultrapermissions.*` nodes exist for administrative actions.

## Delegating groups

In the group GUI, configure which groups may be assigned and the maximum assignment duration (grant groups). A user with such an active group receives limited `/upc` access:

- `AddGroup`: only approved groups and allowed durations.
- `listGroupPermissions`, `listPlayerPermissions`: list viewing.

Delegation does not grant the GUI editor, user creation, suffix editing or other administrative operations. Those require superadmin. An unknown user is denied access without an exception.

## Permission evaluation

Permissions can be assigned to users and groups, inherited, and scoped by world/server/time. `example.node` is a grant; `-example.node` is a denial. Wildcards, group priority, personal permissions, Bukkit defaults/OP and the negative-override setting participate in lookup. For conflicts, inspect the result in the permission logger rather than drawing a conclusion from a single wildcard.

The console and trusted plugins with API access form an administrative trust boundary. A shared cluster HMAC key implies trust in every holder. Players must not have access to the MySQL account or the Registry/mysql.properties/security.properties files.
