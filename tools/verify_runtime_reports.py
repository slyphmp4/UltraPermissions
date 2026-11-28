#!/usr/bin/env python3
"""Verify the complete pinned Paper and Folia matrix against the exact release artifact."""
import hashlib
import json
from pathlib import Path
import sys

from verify_paper import targets_for

reports, plugin, output = map(Path, sys.argv[1:])
plugin_sha = hashlib.sha256(plugin.read_bytes()).hexdigest()
results = {}
folia_results = {}
for platform, platform_results in (("Paper", results), ("Folia", folia_results)):
    prefix = platform.lower()
    for version, target in targets_for(platform).items():
        source = reports / f"{platform}-{version}-runtime" / "verification.json"
        result = json.loads(source.read_text(encoding="utf8"))
        expected = {
            "status": "passed",
            "server_platform": platform,
            f"{prefix}_version": version,
            f"{prefix}_build": target["build"],
            f"{prefix}_sha256": target["sha256"],
            "java_major": target["java"],
            "container_image": target["container_image"],
            "plugin_sha256": plugin_sha,
            "boots": 3,
            "network": "none",
            "persistence_across_restart": True,
            "native_update_folder_handover": True,
            "configured_mysql_failure_stops_server": True,
            "local_rights_unchanged_on_storage_failure": True,
        }
        if platform == "Folia":
            expected.update(connected_player_test=True, distinct_player_regions=True)
        for key, value in expected.items():
            if type(result.get(key)) is not type(value) or result[key] != value:
                raise ValueError(f"{platform} {version}: invalid {key}: {result.get(key)!r}")
        platform_results[version] = result
output.mkdir(parents=True, exist_ok=True)
summary = {"status": "passed", "plugin_sha256": plugin_sha, "targets": results, "folia_targets": folia_results}
(output / "compatibility-verification.json").write_text(json.dumps(summary, indent=2) + "\n", encoding="utf8")
(output / "runtime-verification.json").write_text(json.dumps(results["26.3"], indent=2) + "\n", encoding="utf8")
print(f"Verified one release JAR on {len(results)} pinned Paper and {len(folia_results)} Folia targets.")
