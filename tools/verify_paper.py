#!/usr/bin/env python3
"""Run the pinned Paper compatibility probe in a disposable Docker container."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import time
import urllib.request
import uuid

IMAGE = "eclipse-temurin@sha256:83f4a7fc27df94dcc7b8de04d14cbbba8da14406f921e966969c3b09000aa507"
PAPER_SHA = "ef3dbc68b013deafdbbee3bebf1ba351671ebcda94a8f136809b7532d5ba2fe1"
PAPER_URL = f"https://fill-data.papermc.io/v1/objects/{PAPER_SHA}/paper-26.2-135.jar"


def command(*args, timeout=180, capture=False):
    return subprocess.run(args, check=True, timeout=timeout, text=True,
                          stdout=subprocess.PIPE if capture else None).stdout


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--plugin", type=Path, required=True)
    parser.add_argument("--probe", type=Path, required=True)
    parser.add_argument("--work-dir", type=Path, required=True)
    parser.add_argument("--report-dir", type=Path, required=True)
    parser.add_argument("--accept-eula", action="store_true", help="Accept https://aka.ms/MinecraftEULA for this test server")
    args = parser.parse_args()
    if not args.accept_eula:
        parser.error("This runtime test requires explicit --accept-eula.")
    plugin, probe = args.plugin.resolve(strict=True), args.probe.resolve(strict=True)
    work, reports = args.work_dir.resolve(), args.report_dir.resolve()
    work.mkdir(parents=True, exist_ok=False)
    reports.mkdir(parents=True, exist_ok=True)
    print("Downloading pinned Paper 26.2 build 135...", flush=True)
    request = urllib.request.Request(PAPER_URL, headers={"User-Agent": "UltraPermissions-tests/5.6.9 (https://github.com/slyphmp4/UltraPermissions)"})
    with urllib.request.urlopen(request, timeout=60) as response:
        data = response.read()
    if hashlib.sha256(data).hexdigest() != PAPER_SHA:
        raise RuntimeError("Paper SHA-256 mismatch")
    (work / "paper.jar").write_bytes(data)
    command("docker", "pull", IMAGE)
    base = ["--hostname", "localhost", "--memory", "2300m", "--cpus", "2",
            "--pids-limit", "256", "--cap-drop", "ALL", "--security-opt", "no-new-privileges",
            "--mount", f"type=bind,src={work},dst=/data", "--workdir", "/data"]
    if os.name != "nt":
        # Match the bind-mount owner; a root container with dropped capabilities cannot write runner-owned directories.
        base += ["--user", f"{os.getuid()}:{os.getgid()}"]
    # Only the verified Paper bootstrap can access the network. The plugin is installed afterwards.
    print("Preparing server dependencies (no plugin installed)...", flush=True)
    command("docker", "run", "--rm", *base, IMAGE, "java", "-Dpaperclip.patchonly=true", "-jar", "paper.jar")
    (work / "plugins").mkdir(exist_ok=True)
    shutil.copyfile(plugin, work / "plugins/UltraPermissions.jar")
    shutil.copyfile(probe, work / "plugins/RuntimeProbe.jar")
    (work / "eula.txt").write_bytes(b"eula=true\n")
    (work / "server.properties").write_bytes(
        b"online-mode=false\nserver-ip=127.0.0.1\nserver-port=25565\nenable-rcon=false\nenable-query=false\n"
        b"view-distance=2\nsimulation-distance=2\ngenerate-structures=false\nmax-tick-time=60000\n"
        b"level-type=minecraft:flat\ngenerator-settings={\"layers\":[{\"block\":\"minecraft:bedrock\",\"height\":1}],\"biome\":\"minecraft:plains\"}\n"
        b"pause-when-empty-seconds=-1\n")
    def run_boot(boot):
        name = "ultrapermissions-probe-" + uuid.uuid4().hex[:12]
        print(f"Boot {boot}/2: starting real Paper with UltraPermissions; no published host ports or network...", flush=True)
        try:
            command("docker", "run", "--detach", "--name", name, "--network", "none", *base,
                    IMAGE, "java", "-Xms512M", "-Xmx1536M", "-jar", "paper.jar", "--nogui")
            deadline = time.monotonic() + 150
            while time.monotonic() < deadline:
                state = json.loads(command("docker", "inspect", "--format", "{{json .State}}", name, capture=True))
                if not state["Running"]:
                    if state["ExitCode"] != 0 or state["OOMKilled"]:
                        raise RuntimeError(f"Boot {boot}: Paper exited unsuccessfully: {state}")
                    break
                time.sleep(2)
            else:
                raise TimeoutError(f"Boot {boot}: Runtime probe exceeded 150 seconds")
            result_path = work / "plugins/RuntimeProbe/verification.json"
            result = json.loads(result_path.read_text(encoding="utf8"))
            min_checks = 15 if boot == 1 else 10
            if (result.get("status") != "passed" or result.get("boot") != boot
                    or result.get("assertions", 0) < min_checks
                    or result.get("server") != "26.2.build.135-stable"):
                raise RuntimeError(f"Boot {boot} failed or did not run expected checks: {result}")
            (reports / f"boot-{boot}-verification.json").write_text(
                json.dumps(result, indent=2) + "\n", encoding="utf8")
            return result
        finally:
            logs = subprocess.run(["docker", "logs", name], capture_output=True, text=True)
            (reports / f"server-boot-{boot}.log").write_text(logs.stdout + logs.stderr, encoding="utf8")
            subprocess.run(["docker", "rm", "--force", name], check=False, stdout=subprocess.DEVNULL)

    first = run_boot(1)
    if not (work / "plugins/RuntimeProbe/phase-one.marker").exists():
        raise RuntimeError("Boot 1 did not leave an authenticated completion marker")
    second = run_boot(2)
    result = {
        "status": "passed",
        "assertions": first["assertions"] + second["assertions"],
        "boots": 2,
        "boot_assertions": [first["assertions"], second["assertions"]],
        "server": first["server"],
        "java": second["java"],
        "paper_sha256": PAPER_SHA,
        "plugin_sha256": hashlib.sha256(plugin.read_bytes()).hexdigest(),
        "container_image": IMAGE,
        "network": "none",
        "connected_player_test": False,
        "persistence_across_restart": True,
    }
    (reports / "verification.json").write_text(json.dumps(result, indent=2) + "\n", encoding="utf8")
    print(json.dumps(result, indent=2), flush=True)


if __name__ == "__main__":
    main()
