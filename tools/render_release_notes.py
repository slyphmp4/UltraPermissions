#!/usr/bin/env python3
"""Render EN, UK and RU release notes with links valid on a GitHub release page."""
from pathlib import Path
import re
import sys
from urllib.parse import quote

root = Path(__file__).resolve().parent.parent
version, output = sys.argv[1:]
parts = ["[English](#english) · [Українська](#українська) · [Русский](#русский)\n"]
for label, suffix in [("English", ""), ("Українська", ".uk"), ("Русский", ".ru")]:
    source = root / "docs/releases" / f"{version}{suffix}.md"
    body = "\n".join(source.read_text(encoding="utf8").splitlines()[2:])

    def link(match):
        text, target = match.groups()
        if re.match(r"[a-z]+:|#", target):
            return match.group(0)
        path = (source.parent / target).resolve()
        if not path.is_file():
            raise ValueError(f"Broken release-note link: {target}")
        relative = path.relative_to(root).as_posix()
        return f"[{text}](https://github.com/slyphmp4/UltraPermissions/blob/v{quote(version)}/{quote(relative)})"

    parts.append(f"## {label}\n\n" + re.sub(r"\[([^\]]+)\]\(([^)]+)\)", link, body) + "\n")
Path(output).write_text("\n".join(parts), encoding="utf8")
