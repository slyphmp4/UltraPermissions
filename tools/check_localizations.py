#!/usr/bin/env python3
"""Verify bundled locale coverage and runtime-safe format markers."""
from __future__ import annotations

import argparse
from collections import Counter
from pathlib import Path
import re
import sys
import zipfile

ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOT = ROOT / "src/main/java/me/TechsCode/UltraPermissions"
RESOURCES = ROOT / "src/main/resources/translations"
LANGUAGES = ("Russian", "Ukrainian")
PARTS = ("Base", "UltraPermissions")
PHRASE = re.compile(r'Phrase\.create\(\s*"([^"]+)"\s*,\s*"((?:\\.|[^"\\])*)"')
LINE = re.compile(r'^([^#:]+): "(.*)"$')
TOKEN = re.compile(r'%[A-Za-z0-9_]+%|\{[A-Za-z][^{}]*\}')
LATIN = re.compile(r'[A-Za-z][A-Za-z0-9]*')
ALLOWED_LATIN = {
    "MySQL", "Shift", "Insane", "Editor", "Discord", "SSL",
    "none", "ID", "UltraPermissions", "Spigot", "yml", "UUID",
    "BungeeCord", "URL", "promote", "demote", "NameTagEdit",
    "InsaneEditor", "help", "Bukkit", "Q", "m", "h", "d",
}


def read_sources(errors: list[str]) -> dict[str, dict[str, str]]:
    result = {part: {} for part in PARTS}
    for path in JAVA_ROOT.rglob("*.java"):
        relative = path.relative_to(JAVA_ROOT)
        part = "Base" if relative.parts[0] == "base" else "UltraPermissions"
        content = path.read_text(encoding="utf-8")
        for match in PHRASE.finditer(content):
            key, original = match.groups()
            if key in result[part]:
                errors.append(f"Duplicate source key {key} in {path}")
            result[part][key] = original
    overlapping = result["Base"].keys() & result["UltraPermissions"].keys()
    for key in sorted(overlapping):
        errors.append(f"Source key appears in both namespaces: {key}")
    return result


def read_locale(path: Path, errors: list[str]) -> dict[str, str]:
    entries: dict[str, str] = {}
    if not path.is_file():
        errors.append(f"Missing language resource: {path}")
        return entries
    for lineno, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        match = LINE.fullmatch(line)
        if not match:
            errors.append(f"{path}:{lineno}: malformed translation")
            continue
        key, value = match.groups()
        if key in entries:
            errors.append(f"{path}:{lineno}: duplicate key {key}")
        entries[key] = value
    return entries


def check_translation(
    part: str,
    language: str,
    expected: dict[str, str],
    translated: dict[str, str],
    errors: list[str],
) -> None:
    for key in sorted(expected.keys() - translated.keys()):
        errors.append(f"{part}_{language}: missing {key}")
    for key in sorted(translated.keys() - expected.keys()):
        errors.append(f"{part}_{language}: unknown {key}")
    for key in expected.keys() & translated.keys():
        english, value = expected[key], translated[key]
        if Counter(TOKEN.findall(english)) != Counter(TOKEN.findall(value)):
            errors.append(f"{part}_{language}: broken placeholder in {key}")
        if english.count("**") != value.count("**"):
            errors.append(f"{part}_{language}: broken color markers in {key}")
        if not value.strip():
            errors.append(f"{part}_{language}: empty value for {key}")
        without_tokens = TOKEN.sub("", value)
        without_urls = re.sub(r'https?://[^\s*]+', "", without_tokens)
        unexpected = sorted(set(LATIN.findall(without_urls)) - ALLOWED_LATIN)
        if unexpected:
            errors.append(f"{part}_{language}: untranslated Latin words in {key}: {unexpected}")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", type=Path, help="Also inspect packaged JAR resources")
    args = parser.parse_args()
    errors: list[str] = []
    expected = read_sources(errors)
    locales = {}
    for part in PARTS:
        for language in LANGUAGES:
            name = f"{part}_{language}.lang"
            path = RESOURCES / name
            values = read_locale(path, errors)
            locales[name] = path
            check_translation(part, language, expected[part], values, errors)

    if args.jar:
        if not args.jar.is_file():
            errors.append(f"Missing plugin JAR: {args.jar}")
        else:
            with zipfile.ZipFile(args.jar) as jar:
                for name, path in locales.items():
                    member = f"translations/{name}"
                    if member not in jar.namelist():
                        errors.append(f"JAR does not contain {member}")
                    elif jar.read(member) != path.read_bytes():
                        errors.append(f"JAR resource differs from source: {member}")

    for error in errors:
        print("ERROR:", error, file=sys.stderr)
    if errors:
        print(f"Locale checks failed: {len(errors)} issue(s)", file=sys.stderr)
        return 1
    total = sum(len(x) for x in expected.values())
    print(f"Locale checks passed: {total} keys in each of {len(LANGUAGES)} languages; placeholders, colors, JAR verified.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
