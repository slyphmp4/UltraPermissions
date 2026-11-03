#!/usr/bin/env python3
"""Explicit maintenance operation: rebuild the pinned, relocated runtime bundle.

Normal Gradle builds never download dependencies or run this script. Inputs are
locked by SHA-256 in config/runtime-components.json. Java class constant-pool
UTF8 entries are relocated without changing executable instructions/indices.
"""
import argparse
import hashlib
import io
import json
from pathlib import Path
import struct
import urllib.request
import zipfile

ROOT = Path(__file__).resolve().parents[1]
PREFIX = "me/TechsCode/UltraPermissions/dependencies/"


def remap(data, mappings):
    for original, target in sorted(mappings.items(), key=lambda pair: -len(pair[0])):
        data = data.replace(original.encode(), target.encode())
        data = data.replace(original.replace("/", ".").encode(), target.replace("/", ".").encode())
    return data


def relocate_class(data, mappings):
    if data[:4] != b"\xca\xfe\xba\xbe":
        raise ValueError("Not a Java class")
    result = bytearray(data[:10])
    count = struct.unpack_from(">H", data, 8)[0]
    offset, index = 10, 1
    while index < count:
        tag = data[offset]
        result.append(tag)
        offset += 1
        if tag == 1:
            size = struct.unpack_from(">H", data, offset)[0]
            offset += 2
            text = remap(data[offset:offset + size], mappings)
            result += struct.pack(">H", len(text)) + text
            offset += size
        else:
            size = {3: 4, 4: 4, 5: 8, 6: 8, 7: 2, 8: 2, 9: 4, 10: 4,
                    11: 4, 12: 4, 15: 3, 16: 2, 17: 4, 18: 4, 19: 2, 20: 2}[tag]
            result += data[offset:offset + size]
            offset += size
            if tag in (5, 6):
                index += 1
        index += 1
    return bytes(result) + data[offset:]


def read_input(item, cache, download):
    path = cache / item["filename"]
    if not path.is_file():
        if not download:
            raise RuntimeError(f"Missing {path}; use --download for this maintenance operation")
        request = urllib.request.Request(item["url"], headers={"User-Agent": "UltraPermissions-dependency-maintenance"})
        with urllib.request.urlopen(request, timeout=60) as response:
            data = response.read(32 * 1024 * 1024 + 1)
        if len(data) > 32 * 1024 * 1024:
            raise RuntimeError("Dependency exceeds maintenance download limit")
        if hashlib.sha256(data).hexdigest() != item["sha256"]:
            raise RuntimeError(f"Input checksum mismatch: {item['filename']}")
        path.write_bytes(data)
    data = path.read_bytes()
    if hashlib.sha256(data).hexdigest() != item["sha256"]:
        raise RuntimeError(f"Cached input checksum mismatch: {path}")
    return data


def archive(entries):
    output = io.BytesIO()
    with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as jar:
        for name, data in sorted(entries.items()):
            entry = zipfile.ZipInfo(name, (1980, 1, 1, 0, 0, 0))
            entry.compress_type = zipfile.ZIP_DEFLATED
            entry.external_attr = 0o100644 << 16
            jar.writestr(entry, data)
    return output.getvalue()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--cache-dir", type=Path, default=ROOT / "build/dependency-downloads")
    parser.add_argument("--download", action="store_true")
    args = parser.parse_args()
    args.cache_dir.mkdir(parents=True, exist_ok=True)
    lock = json.loads((ROOT / "config/runtime-components.json").read_text(encoding="utf-8"))
    components = lock["components"]
    mappings = {item["source_package"]: item["target_package"] for item in components}
    replaced = tuple(item["target_package"] + "/" for item in components)
    # Unknown-version Kyori/JetBrains subsets and unchanged Apache HttpClient 4.x
    # are retained byte-for-byte from the separately pinned historical aggregate.
    with zipfile.ZipFile(io.BytesIO(read_input(lock["baseline"], args.cache_dir, args.download))) as baseline:
        classes = {n: baseline.read(n) for n in baseline.namelist()
                   if n.endswith(".class") and not n.startswith(replaced)}
    resources = ROOT / "src/main/resources"
    for item in components:
        with zipfile.ZipFile(io.BytesIO(read_input(item, args.cache_dir, args.download))) as jar:
            for name in jar.namelist():
                if name.endswith("/") or name.startswith("META-INF/versions/") or name == "module-info.class":
                    continue
                data = jar.read(name)
                mapped = remap(name.encode(), mappings).decode()
                if name.endswith(".class"):
                    if not name.startswith(item["source_package"] + "/"):
                        continue
                    classes[mapped] = relocate_class(data, mappings)
                elif name.startswith("META-INF/maven/"):
                    dest = resources / name
                    dest.parent.mkdir(parents=True, exist_ok=True)
                    dest.write_bytes(data)
                elif name.startswith("META-INF/services/"):
                    service = remap(name[len("META-INF/services/"):].encode(), mappings).decode()
                    dest = resources / "META-INF/services" / service
                    dest.parent.mkdir(parents=True, exist_ok=True)
                    dest.write_bytes(remap(data, mappings))
                elif name.startswith("META-INF/") and any(x in name.upper() for x in ("LICENSE", "NOTICE")):
                    dest = resources / "META-INF/component-licenses" / item["artifact"] / Path(name).name
                    dest.parent.mkdir(parents=True, exist_ok=True)
                    dest.write_bytes(data)
                elif name.startswith(item["source_package"] + "/"):
                    dest = resources / mapped
                    dest.parent.mkdir(parents=True, exist_ok=True)
                    dest.write_bytes(data)
    # The NBT API carries its own relocated bStats facade. Keep both facades on
    # the same pinned version instead of silently shipping a second old copy.
    metric_prefix = PREFIX + "nbt/utils/metrics/"
    classes = {n: data for n, data in classes.items() if not n.startswith(metric_prefix)}
    for item in components:
        if item["artifact"] not in ("bstats-base", "bstats-bukkit"):
            continue
        metric_map = {"org/bstats": metric_prefix.rstrip("/")}
        with zipfile.ZipFile(io.BytesIO(read_input(item, args.cache_dir, args.download))) as jar:
            for name in jar.namelist():
                if name.startswith("org/bstats/") and name.endswith(".class"):
                    classes[remap(name.encode(), metric_map).decode()] = relocate_class(jar.read(name), metric_map)
    bundle = archive(classes)
    folder = ROOT / "libs/repository/me/techscode/ultrapermissions/runtime-libraries" / lock["bundle_version"]
    folder.mkdir(parents=True, exist_ok=True)
    for suffix in ("", "-compile-api"):
        (folder / f"runtime-libraries-{lock['bundle_version']}{suffix}.jar").write_bytes(bundle)
    (folder / f"runtime-libraries-{lock['bundle_version']}.pom").write_text(
        '<project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion>'
        '<groupId>me.techscode.ultrapermissions</groupId><artifactId>runtime-libraries</artifactId>'
        f'<version>{lock["bundle_version"]}</version><description>Pinned relocated runtime libraries</description></project>\n', encoding="utf-8")
    print(f"Rebuilt {len(classes)} library classes; SHA-256 {hashlib.sha256(bundle).hexdigest()}")


if __name__ == "__main__":
    main()
