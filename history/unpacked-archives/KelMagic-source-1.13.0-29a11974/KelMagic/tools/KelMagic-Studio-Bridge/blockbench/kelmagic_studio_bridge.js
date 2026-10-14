// KelMagic Studio Bridge plugin for Blockbench Desktop.
// Sends only a small local report of the active project to 127.0.0.1; it does not upload models or textures.
const KELMAGIC_BRIDGE_URL = 'http://127.0.0.1:18765/blockbench/status';
const KELMAGIC_BRIDGE_TOKEN = 'kelmagic-studio-bridge-local-v1';
let kelmagicHeartbeat = null;
let kelmagicAction = null;

function kelmagicSafeCount(value) {
  try { return Array.isArray(value) ? value.length : 0; } catch (_) { return 0; }
}
function kelmagicReport() {
  let selectedCount = null;
  try { selectedCount = Array.isArray(Outliner.selected) ? Outliner.selected.length : null; } catch (_) {}
  let textures = [];
  try {
    textures = (Texture.all || []).map(t => ({
      name: t.name || t.path || 'unnamed',
      width: t.width || null,
      height: t.height || null
    })).slice(0, 100);
  } catch (_) {}
  let projectPath = null;
  try { projectPath = Project && (Project.save_path || Project.export_path) ? (Project.save_path || Project.export_path) : null; } catch (_) {}
  return {
    timestamp: new Date().toISOString(),
    projectName: (typeof Project !== 'undefined' && Project && Project.name) ? Project.name : null,
    projectPath: projectPath,
    formatId: (typeof Format !== 'undefined' && Format && Format.id) ? Format.id : null,
    formatName: (typeof Format !== 'undefined' && Format && Format.name) ? Format.name : null,
    cubeCount: (typeof Cube !== 'undefined' && Cube && Cube.all) ? kelmagicSafeCount(Cube.all) : 0,
    groupCount: (typeof Group !== 'undefined' && Group && Group.all) ? kelmagicSafeCount(Group.all) : 0,
    textureCount: textures.length,
    textures,
    selectedCount
  };
}
async function kelmagicSendStatus(showToast) {
  try {
    const response = await fetch(KELMAGIC_BRIDGE_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'X-KelMagic-Token': KELMAGIC_BRIDGE_TOKEN },
      body: JSON.stringify(kelmagicReport())
    });
    if (!response.ok) throw new Error('HTTP ' + response.status);
    if (showToast && typeof Blockbench !== 'undefined') Blockbench.showQuickMessage('KelMagic: model status sent to Cline');
    return true;
  } catch (error) {
    if (showToast && typeof Blockbench !== 'undefined') Blockbench.showQuickMessage('KelMagic bridge offline — restart Cline MCP server first');
    return false;
  }
}

Plugin.register('kelmagic_studio_bridge', {
  title: 'KelMagic Studio Bridge',
  author: 'KelMagic',
  description: 'Reports the active Blockbench model metadata to the local KelMagic MCP bridge.',
  icon: 'share',
  version: '0.1.0',
  variant: 'desktop',
  onload() {
    kelmagicAction = new Action('kelmagic_studio_bridge_report', {
      name: 'KelMagic: Send Model Status',
      description: 'Send the current Blockbench project summary to the local Cline bridge',
      icon: 'share',
      click() { kelmagicSendStatus(true); }
    });
    MenuBar.menus.tools.addAction(kelmagicAction);
    kelmagicHeartbeat = setInterval(() => kelmagicSendStatus(false), 5000);
    kelmagicSendStatus(false);
  },
  onunload() {
    if (kelmagicHeartbeat) clearInterval(kelmagicHeartbeat);
    if (kelmagicAction) kelmagicAction.delete();
  }
});
