# Интеграции и placeholders

Vault подключает permissions/chat provider; PlaceholderAPI и MVdWPlaceholderAPI позволяют использовать данные в совместимых плагинах. NametagEdit обслуживает отображение имён/префиксов при наличии интеграции. Устанавливайте эти плагины отдельно, когда они нужны; compile-only API из исходного проекта копировать на сервер не нужно.

## PlaceholderAPI

Identifier: `uperms`. Базовые placeholders:

| Placeholder | Назначение |
|---|---|
| `%uperms_prefix%` | Player Prefix or Prefix of 1. Group |
| `%uperms_prefix_color%` | Color of Player Prefix or Prefix of 1. Group |
| `%uperms_prefixes%` | Replaced with player prefix & group prefixes |
| `%uperms_rank%` | Group Name of 1. Group |
| `%uperms_rank_timer%` | Rank Timer of next highest timed group |
| `%uperms_rank_timer_detailed%` | Detailed Rank Timer of next highest timed group |
| `%uperms_ranks%` | Group Name of all Groups |
| `%uperms_secondaryprefix%` | Prefix of 2. Group |
| `%uperms_secondarysuffix%` | Suffix of 2. Group |
| `%uperms_suffix%` | Player Suffix or Suffix of 1. Group |
| `%uperms_suffix_color%` | Color of Player Suffix or Suffix of 1. Group |
| `%uperms_suffixes%` | Replaced with all group suffixes |

Встроенный формат шаблонов плагина — `{Prefix}`, `{Rank}`, `{Rank_timer}` и соответствующие имена из списка, с учётом конкретного места использования. PlaceholderAPI-запрос должен иметь пользователя; значения зависят от мира/сервера и действующих групп.

Дополнительные query-формы обработчика: `has_group_<group>`, `has_permission_<node>`, `permission_timer_<node>`, `inherits_group_<group>`, `rank_timer_<group>`. Они требуют существующего пользователя/объекта; интеграционный плагин определяет контекст запроса. Полный Minecraft/placeholder runtime не исполняется в офлайн CI.
