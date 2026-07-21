import urllib.request, urllib.error, json, base64, os, sys

with open("C:/Users/q0214/.openclaw/awa.txt", "rb") as f:
    token = f.read().decode("utf-8").strip()

api = "https://api.github.com/repos/q02144235/HyperNotice"

def req(method, path, data=None):
    url = api + path
    d = json.dumps(data).encode() if data else None
    r = urllib.request.Request(url, data=d, method=method)
    r.add_header("Authorization", "Bearer " + token)
    r.add_header("Accept", "application/vnd.github+json")
    r.add_header("Content-Type", "application/json")
    try:
        resp = urllib.request.urlopen(r, timeout=60)
        return json.loads(resp.read().decode())
    except urllib.error.HTTPError as e:
        body = e.read().decode()[:300]
        print(f"HTTP {e.code} {path}: {body}")
        sys.exit(1)

# 1. 检查仓库
info = req("GET", "")
print(f"OK: {info['full_name']}")

root = "F:/HyperNotice"
files = [
    "build.gradle.kts",
    "settings.gradle.kts",
    "gradle.properties",
    "gradle/wrapper/gradle-wrapper.properties",
    "gradlew",
    "app/build.gradle.kts",
    "app/src/main/AndroidManifest.xml",
    "app/src/main/kotlin/com/example/hypernotice/SettingsActivity.kt",
    "app/src/main/kotlin/com/example/hypernotice/HyperNoticeUI.kt",
]

items = []
for fp in files:
    full = os.path.join(root, fp.replace("/", "\\"))
    if not os.path.exists(full):
        print(f"SKIP: {fp}")
        continue
    with open(full, "rb") as f:
        b64 = base64.b64encode(f.read()).decode()
    blob = req("POST", "/git/blobs", {"content": b64, "encoding": "base64"})
    items.append({"path": fp, "mode": "100644", "type": "blob", "sha": blob["sha"]})
    print(f"BLOB: {fp}")

# 3. Tree
tree = req("POST", "/git/trees", {"tree": items})
print(f"TREE: {tree['sha'][:12]}")

# 4. Commit
commit = req("POST", "/git/commits", {
    "message": "HyperNotice: MiUIX UI initial",
    "tree": tree["sha"],
    "parents": []
})
print(f"COMMIT: {commit['sha'][:12]}")

# 5. Branch
ref = req("POST", "/git/refs", {
    "ref": "refs/heads/main",
    "sha": commit["sha"]
})
print(f"BRANCH: {ref['object']['sha'][:12]}")

# 6. Workflow via Contents API
wf_path = os.path.join(root, ".github", "workflows", "main.yml")
if os.path.exists(wf_path):
    with open(wf_path, "rb") as f:
        wfb64 = base64.b64encode(f.read()).decode()
    wf = req("PUT", "/contents/.github%2Fworkflows%2Fmain.yml", {
        "message": "Add workflow",
        "content": wfb64,
        "branch": "main"
    })
    print(f"WORKFLOW: {wf['commit']['sha'][:12]}")

print("\n🎉 ALL DONE! Actions will start building now.")
