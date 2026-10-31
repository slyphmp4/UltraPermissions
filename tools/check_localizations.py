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
PHRASE_FIELD = re.compile(
    r'(?m)^[ \t]*((?:(?:private|public|protected|static|final|volatile|transient)\s+)+)'
    r'Phrase\s+(\w+)\s*=\s*Phrase\.create\s*\('
)
LINE = re.compile(r'^([^#:]+): "(.*)"$')
TOKEN = re.compile(r'%[A-Za-z0-9_]+%|\{[A-Za-z][^{}]*\}')
LATIN = re.compile(r'[A-Za-z][A-Za-z0-9]*')
ALLOWED_LATIN = {
    "MySQL", "Shift", "Insane", "Editor", "Discord", "SSL",
    "none", "ID", "UltraPermissions", "Spigot", "yml", "UUID",
    "BungeeCord", "URL", "promote", "demote", "NameTagEdit",
    "InsaneEditor", "help", "Bukkit", "JAR", "GitHub", "Q", "m", "h", "d",
}


def read_sources(errors: list[str]) -> dict[str, dict[str, str]]:
    result = {part: {} for part in PARTS}
    for path in JAVA_ROOT.rglob("*.java"):
        relative = path.relative_to(JAVA_ROOT)
        part = "Base" if relative.parts[0] == "base" else "UltraPermissions"
        content = path.read_text(encoding="utf-8")
        for field in PHRASE_FIELD.finditer(content):
            modifiers, name = field.groups()
            if "static" not in modifiers.split():
                errors.append(f"Unregistered non-static Phrase {name} in {path}")
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


def check_entity_names(errors: list[str]) -> dict[str, Path]:
    """The entity picker must have a translated label for every enum member."""
    source = (JAVA_ROOT / "base/item/XEntityType.java").read_text(encoding="utf-8")
    declaration = source.split("public static final XRegistry", 1)[0]
    names = {name.lower() for name in re.findall(r"^\s*([A-Z][A-Z0-9_]*)\s*\(", declaration, re.M)}
    resources: dict[str, Path] = {}
    for language in LANGUAGES:
        file_name = f"entities_{language}.properties"
        path = RESOURCES / file_name
        resources[file_name] = path
        if not path.is_file():
            errors.append(f"Missing entity name resource: {path}")
            continue
        keys: set[str] = set()
        for lineno, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
            if not line.strip() or line.startswith("#"):
                continue
            if "=" not in line:
                errors.append(f"{path}:{lineno}: malformed entity label")
                continue
            key, value = line.split("=", 1)
            if key in keys:
                errors.append(f"{path}:{lineno}: duplicate entity name: {key}")
            keys.add(key)
            if not value.strip():
                errors.append(f"{path}:{lineno}: empty entity name: {key}")
            if LATIN.search(value):
                errors.append(f"{path}:{lineno}: untranslated entity name: {key}: {value}")
        for missing in sorted(names - keys):
            errors.append(f"{path}: missing entity name: {missing}")
        for extra in sorted(keys - names):
            errors.append(f"{path}: unknown entity name: {extra}")
    return resources


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", type=Path, help="Also inspect packaged JAR resources")
    args = parser.parse_args()
    errors: list[str] = []
    # TranslationFile must accept valid keys containing underscores (e.g. rank_timer).
    loader_source = (JAVA_ROOT / "base/translations/TranslationFile.java").read_text(encoding="utf-8")
    if '.filter(var0 -> !var0[0].contains("_"))' in loader_source:
        errors.append("Runtime language loader silently rejects keys containing underscores")
    if 'StandardCharsets.UTF_8' not in loader_source:
        errors.append("Runtime language loader must decode bundled UTF-8 locale files")
    expected = read_sources(errors)
    locales = {}
    for part in PARTS:
        for language in LANGUAGES:
            name = f"{part}_{language}.lang"
            path = RESOURCES / name
            values = read_locale(path, errors)
            locales[name] = path
            check_translation(part, language, expected[part], values, errors)

    entity_resources = check_entity_names(errors)
    if args.jar:
        if not args.jar.is_file():
            errors.append(f"Missing plugin JAR: {args.jar}")
        else:
            with zipfile.ZipFile(args.jar) as jar:
                for name, path in {**locales, **entity_resources}.items():
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
