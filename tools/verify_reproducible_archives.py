#!/usr/bin/env python3
"""Fail if two independently produced ZIP/JAR files differ; explain why."""
from __future__ import annotations

import argparse
import hashlib
from pathlib import Path
import sys
from zipfile import ZipFile


def sha(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def compare(left: Path, right: Path) -> bool:
    a, b = left.read_bytes(), right.read_bytes()
    if a == b:
        print(f"Reproducible: {left.name}, SHA-256 {sha(a)}")
        return True

    print(f"NOT reproducible: {left.name} ({len(a)} bytes) vs {right.name} ({len(b)} bytes)", file=sys.stderr)
    with ZipFile(left) as z1, ZipFile(right) as z2:
        i1 = {item.filename: item for item in z1.infolist()}
        i2 = {item.filename: item for item in z2.infolist()}
        missing_left, missing_right = sorted(i2.keys() - i1.keys()), sorted(i1.keys() - i2.keys())
        if missing_left:
            print(f"  Only in right: {missing_left[:12]}", file=sys.stderr)
        if missing_right:
            print(f"  Only in left: {missing_right[:12]}", file=sys.stderr)
        differences = 0
        for name in sorted(i1.keys() & i2.keys()):
            if z1.read(name) != z2.read(name):
                print(f"  Different entry bytes: {name} ({sha(z1.read(name))[:16]} vs {sha(z2.read(name))[:16]})", file=sys.stderr)
                differences += 1
                if differences >= 25:
                    break
        if not differences and not missing_left and not missing_right:
            print("  Uncompressed entries match; ZIP metadata, ordering or compression differs.", file=sys.stderr)
            for name in sorted(i1):
                x, y = i1[name], i2[name]
                fields = ("date_time", "create_system", "external_attr", "compress_type", "compress_size", "header_offset")
                changed = [field for field in fields if getattr(x, field) != getattr(y, field)]
                if changed:
                    print(f"  ZIP metadata differs: {name}: {changed}", file=sys.stderr)
                    break
    return False


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("pairs", type=Path, nargs="+", help="Linux JAR, Windows JAR, ...")
    args = parser.parse_args()
    if len(args.pairs) % 2:
        parser.error("Expected pairs of paths: left.jar right.jar")
    passed = True
    for offset in range(0, len(args.pairs), 2):
        passed = compare(args.pairs[offset], args.pairs[offset + 1]) and passed
    return 0 if passed else 1


if __name__ == "__main__":
    raise SystemExit(main())
