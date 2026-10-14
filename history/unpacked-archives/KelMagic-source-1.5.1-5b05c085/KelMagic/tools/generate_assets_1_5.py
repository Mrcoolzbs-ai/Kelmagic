"""Generate original garment/UI/icons first, then vanilla-shaped purple fire. Requires Pillow and Gradle Minecraft artifacts."""
from pathlib import Path
import subprocess,sys
root=Path(__file__).resolve().parents[1]
for script in ['generate_stage3_assets.py','generate_revision15_fire.py']:
 subprocess.run([sys.executable,str(root/'tools'/script)],cwd=root,check=True)
