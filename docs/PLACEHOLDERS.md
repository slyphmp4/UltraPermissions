[English](PLACEHOLDERS.md) · [Українська](PLACEHOLDERS.uk.md) · [Русский](PLACEHOLDERS.ru.md)

# Integrations and placeholders

Vault provides the permissions/chat integration; PlaceholderAPI and MVdWPlaceholderAPI expose data to compatible plugins. NametagEdit handles name/prefix display when the integration is present. Install these plugins separately when needed; do not copy compile-only API files from the source project onto the server.

## PlaceholderAPI

Identifier: `uperms`. Base placeholders:

| Placeholder | Purpose |
|---|---|
| `%uperms_prefix%` | Player prefix, or the first group's prefix |
| `%uperms_prefix_color%` | Color of the player prefix, or the first group's prefix |
| `%uperms_prefixes%` | Player prefix combined with group prefixes |
| `%uperms_rank%` | Name of the first group |
| `%uperms_rank_timer%` | Timer of the next highest temporary group |
| `%uperms_rank_timer_detailed%` | Detailed timer of the next highest temporary group |
| `%uperms_ranks%` | Names of all groups |
| `%uperms_secondaryprefix%` | Prefix of the second group |
| `%uperms_secondarysuffix%` | Suffix of the second group |
| `%uperms_suffix%` | Player suffix, or the first group's suffix |
| `%uperms_suffix_color%` | Color of the player suffix, or the first group's suffix |
| `%uperms_suffixes%` | Combined suffixes of all groups |

The plugin's native template format uses `{Prefix}`, `{Rank}`, `{Rank_timer}` and the corresponding names from the list, depending on the place of use. A PlaceholderAPI request must have a user context; values depend on the world/server and active groups.

Additional query forms supported by the handler: `has_group_<group>`, `has_permission_<node>`, `permission_timer_<node>`, `inherits_group_<group>`, `rank_timer_<group>`. They require an existing user/object; the integration plugin supplies the request context. The full Minecraft/placeholder runtime is not executed in offline CI.
