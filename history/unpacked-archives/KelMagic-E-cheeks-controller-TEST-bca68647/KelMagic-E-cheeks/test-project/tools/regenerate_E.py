from pathlib import Path
import subprocess,sys,shutil
p=Path(__file__).resolve().parents[1];e=p.parent/'experiment'
subprocess.run([sys.executable,str(e/'tools/generate_e2.py')],check=True)
s=e/'states/E0-neutral'
shutil.copy2(s/'red_mage.geo.json',p/'src/main/resources/assets/kelmagic/geo/red_mage.geo.json')
shutil.copy2(s/'E0-neutral.bbmodel',p/'art/red-mage/red_mage_gecko.bbmodel')
shutil.copy2(s/'red_mage.geo.json',p/'art/red-mage/red_mage.geo.json')
print('E rest geometry installed; PNG and animation clips not modified')
