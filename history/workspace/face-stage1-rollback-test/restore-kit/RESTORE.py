from pathlib import Path
import argparse,json,hashlib
ap=argparse.ArgumentParser(description="Static face stage 1 source rollback; dry run unless --apply")
ap.add_argument("--project",type=Path,required=True);ap.add_argument("--apply",action="store_true");a=ap.parse_args()
root=a.project.resolve();here=Path(__file__).resolve().parent;m=json.loads((here/"rollback-manifest.json").read_text())
assert "mod_version=1.23.0" in (root/"gradle.properties").read_text(),"Wrong project version"
for name,v in m["files"].items():
 p=(root/name).resolve();assert p.is_relative_to(root),"Unsafe path"
 if not p.exists() and v["before"] is None:continue
 assert p.is_file()and hashlib.sha256(p.read_bytes()).hexdigest()==v["after"],"Subsequent change detected: "+name
 if v["before"] is not None:assert hashlib.sha256((here/"before"/name).read_bytes()).hexdigest()==v["before"],"Damaged rollback file"
print("PASS: version and every affected file verified")
if not a.apply:print("Dry run only. Repeat with --apply to restore original source.")
else:
 for name,v in m["files"].items():
  p=root/name
  if v["before"] is None:
   if p.exists():p.unlink()
  else:p.write_bytes((here/"before"/name).read_bytes())
 print("Restored original 1.23.0 source. Replace installed candidate JAR with original JAR separately.")
