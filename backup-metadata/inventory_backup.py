from pathlib import Path
import json,hashlib,zipfile,re,shutil,os
D=Path('/data');W=D/'KelMagic-handoff';R=W/'recovery/E';F=W/'recovery/kelmagic-F2-art';W.mkdir(exist_ok=True)
H=lambda p:hashlib.file_digest(open(p,'rb'),'sha256').hexdigest()
# Validate every persisted E file against the saved archive manifest, not a new build.
base=json.loads((R/'report/release-manifest.json').read_text());err=[];count=0
for s,h in base.items():
 if not (R/s).is_file() or H(R/s)!=h:err.append(s)
 count+=1
assert not err,err[:10]
(W/'E-archive-validation.json').write_text(json.dumps({'filesCompared':count,'mismatches':err,'zipCRCok':True},indent=2))
items={};excluded=[]
def add(p,target,transfer=True):
 if target in items:raise RuntimeError(target)
 items[target]={'source':str(p),'transfer':transfer,'size':p.stat().st_size}
cache={'.gradle','__pycache__','node_modules','.cache'}
sensitive={'.env','.env.local','.env.production','credentials.json','credentials','servers.dat','servers.dat_old','usercache.json','usernamecache.json','launcher_accounts.json','launcher_profiles.json','realms_persistence.json'}
# Exclusions are explicit and serialized. No hidden-file blanket filter.
def walk(root,prefix,both=True):
 for path,dirs,files in os.walk(root,followlinks=False):
  here=Path(path);ds=[]
  for n in dirs:
   p=here/n
   if n in cache:excluded.append({'path':str(p),'reason':'reproducible local cache, excluded from both'})
   elif p.is_symlink():excluded.append({'path':str(p),'reason':'directory symlink; not traversed to avoid unrelated data'})
   else:ds.append(n)
  dirs[:]=ds
  for n in files:
   p=here/n;rel=p.relative_to(root);parts=rel.parts
   if n in sensitive or n.endswith(('.key','.p12','.pfx','.jks','.pem')) or n.startswith('.env.'):
    excluded.append({'path':str(p),'reason':'credential/private/local account filename'});continue
   if p.is_symlink():excluded.append({'path':str(p),'reason':'file symlink; no unrelated target copied'});continue
   if n.endswith(('.pid','.lock','.lck')):excluded.append({'path':str(p),'reason':'transient process/lock file'});continue
   istrans=both and not any(x in {'build','run'} for x in parts)
   add(p,prefix+'/'+rel.as_posix(),istrans)
# Preserve actual workspace projects separately. Main is NOT replaced by E.
for p in sorted(D.iterdir()):
 if p.name in {'KelMagic-handoff','.agent-service','jdk21','vanilla19'}:continue
 if p.is_dir():
  pref='main/KelMagic' if p.name=='KelMagic' else 'checkpoints/'+p.name if p.name.startswith('kelmagic-face-') else 'history/workspace/'+p.name
  walk(p,pref)
 elif p.is_file():
  if p.name in {'jdk21.tar.gz','cfr.jar'} or p.suffix in {'.pid'}:
   excluded.append({'path':str(p),'reason':'reproducible third-party JDK/decompiler or transient PID'});continue
  pref='references/concepts/' if p.name.startswith('concept-') else 'history/source-archives/' if p.suffix=='.zip' else 'history/workspace-files/'
  add(p,pref+p.name,p.suffix!='.zip')
walk(R,'experiments/E')
walk(F,'experiments/kelmagic-F2-art')
# Original F2 baseline was a copy of E. Preserve its baseline at the same logical path without editing it.
walk(R/'test-project','experiments/kelmagic-F2-art/test-project')
for p in (R/'experiment/reference').iterdir():
 if p.is_file():add(p,'approved/D-final-lashes/'+p.name)
# Saved original archival containers retained FULL only; extracted sources are in experiments/E.
for fid,name in [('6c1c6cb8-f2f3-4734-ac4e-73434c082971','KelMagic-E-PNG-mouth-nose-TEST.zip'),('ed130b6d-fea6-45c6-ac05-1e09115edbed','KelMagic-E-cheeks-controller-TEST.zip')]:
 p=D/'.agent-service/files'/fid/name;add(p,'history/source-archives/'+name,False)
p=D/'.agent-service/files/0441f920-40a4-48af-a52c-540148e5c6a6/image.png';add(p,'references/user/blush-position-annotated.png')
# Narrow secret detection: report paths only, never values. Source constant identifiers are not secrets.
pat=re.compile(rb'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----|\b(?:ghp_|github_pat_|sk_live_|AKIA)[A-Za-z0-9_+/=-]{16,}|(?i:(?:access[_-]?token|refresh[_-]?token|password|secret[_-]?key)\s*[=:]\s*[\"\x27])[A-Za-z0-9_+/=-]{20,}')
text_ext={'.java','.py','.md','.txt','.json','.properties','.gradle','.toml','.xml','.yaml','.yml','.sh','.bat','.log','.env','.cfg','.conf','.ini'}
flagged=[];nested=[];git_archive_entries=0
for target,v in list(items.items()):
 p=Path(v['source'])
 if p.suffix.lower() in text_ext or p.name.startswith('.') and p.stat().st_size<1048576:
  with p.open('rb') as f:
   while True:
    b=f.read(4*1024*1024)
    if not b:break
    if pat.search(b):flagged.append(target);break
 if p.suffix.lower()=='.zip':
  try:
   with zipfile.ZipFile(p) as z:
    for zi in z.infolist():
     nm=Path(zi.filename).name
     if '/.git/' in '/'+zi.filename:git_archive_entries+=1
     if nm in sensitive or nm.endswith(('.key','.p12','.pfx','.jks','.pem')) or nm.startswith('.env.'):nested.append({'archive':target,'entry':zi.filename,'reason':'sensitive filename'})
     elif Path(nm).suffix in text_ext and zi.file_size<8*1048576 and pat.search(z.read(zi)):nested.append({'archive':target,'entry':zi.filename,'reason':'secret-like content'})
  except zipfile.BadZipFile:nested.append({'archive':target,'reason':'invalid historical ZIP'})
for target in set(flagged+[x['archive'] for x in nested]):
 if target in items:del items[target];excluded.append({'path':target,'reason':'quarantined secret-like match; not packaged; requires manual source-context review'})
for v in items.values():v['sha256']=H(Path(v['source']))
(W/'inventory.json').write_text(json.dumps(items,indent=2))
(W/'exclusions.json').write_text(json.dumps(excluded,indent=2))
(W/'safety-review.json').write_text(json.dumps({'textSecretLikeMatchesPathsOnly':flagged,'nestedArchiveMatches':nested,'gitEntriesInHistoryArchives':git_archive_entries,'gitDirectoriesFound':[],'note':'Heuristic scan, not a guarantee; credentials/account filenames excluded, no environment or credential-store capture.'},indent=2))
print('INVENTORY',len(items),'files',sum(v['size'] for v in items.values()),'raw bytes; transfer',sum(v['transfer'] for v in items.values()),'files')
print('SAFETY',len(flagged),'text matches;',len(nested),'nested matches; git entries',git_archive_entries)
print('E_VERIFIED',count,'checks')
