"""E-only approval-safe entrypoint. Does not run legacy atlas/geometry generators."""
from pathlib import Path
import subprocess,sys
subprocess.run([sys.executable,str(Path(__file__).with_name('regenerate_E.py'))],check=True)
