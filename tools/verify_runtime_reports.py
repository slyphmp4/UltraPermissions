#!/usr/bin/env python3
"""Verify the complete pinned Paper matrix against the exact release artifact."""
import hashlib
import json
from pathlib import Path
import sys

root = Path(__file__).resolve().parent.parent
targets = json.loads((root / "config/paper-targets.json").read_text(encoding="utf8"))
reports, plugin, output = map(Path, sys.argv[1:])
plugin_sha = hashlib.sha256(plugin.read_bytes()).hexdigest()
results = {}
for version, target in targets.items():
    source = reports / f"Paper-{version}-runtime" / "verification.json"
    result = json.loads(source.read_text(encoding="utf8"))
    expected = {
        "status": "passed",
        "paper_version": version,
        "paper_build": target["build"],
        "paper_sha256": target["sha256"],
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
    for key, value in expected.items():
        if type(result.get(key)) is not type(value) or result[key] != value:
            raise ValueError(f"Paper {version}: invalid {key}: {result.get(key)!r}")
    results[version] = result
output.mkdir(parents=True, exist_ok=True)
summary = {"status": "passed", "plugin_sha256": plugin_sha, "targets": results}
(output / "compatibility-verification.json").write_text(json.dumps(summary, indent=2) + "\n", encoding="utf8")
(output / "runtime-verification.json").write_text(json.dumps(results["26.3"], indent=2) + "\n", encoding="utf8")
print(f"Verified one release JAR on all {len(results)} pinned Paper targets.")
