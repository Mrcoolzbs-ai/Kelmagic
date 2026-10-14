import fs from 'node:fs/promises';
import path from 'node:path';
import os from 'node:os';
import http from 'node:http';
import { spawn } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { createRequire } from 'node:module';
import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { z } from 'zod';

const require = createRequire(import.meta.url);
const fsSync = require('node:fs');
const HERE = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(process.env.KELMAGIC_ROOT || path.resolve(HERE, '../..'));
const CONFIG_PATH = process.env.KELMAGIC_CONFIG || path.join(HERE, 'config.json');
const DEFAULT_CONFIG = {
  comfyUIBaseUrl: 'http://127.0.0.1:8188',
  blockbenchExe: '',
  pixeloramaExe: '',
  piskelCliPackageDir: path.join(process.env.APPDATA || path.join(os.homedir(), 'AppData', 'Roaming'), 'npm', 'node_modules', '@ne9roni', 'piskel-cli'),
  bridgeHost: '127.0.0.1',
  bridgePort: 18765,
  blockbenchToken: 'kelmagic-studio-bridge-local-v1'
};
let config = { ...DEFAULT_CONFIG };
let latestBlockbenchReport = null;

try {
  const loaded = JSON.parse((await fs.readFile(CONFIG_PATH, 'utf8')).replace(/^\uFEFF/, ''));
  config = { ...config, ...loaded };
} catch {
  // The bridge still runs with defaults if setup.ps1 has not been run yet.
}

function jsonText(value) {
  return typeof value === 'string' ? value : JSON.stringify(value, null, 2);
}
function result(value, isError = false) {
  return { content: [{ type: 'text', text: jsonText(value) }], ...(isError ? { isError: true } : {}) };
}
function addTool(name, description, schema, handler) {
  server.tool(name, description, schema, async (args) => {
    try {
      return result(await handler(args));
    } catch (err) {
      return result({ error: err?.message || String(err), tool: name }, true);
    }
  });
}
function ensureUnderRoot(absPath) {
  const rel = path.relative(ROOT, absPath);
  if (rel === '' || (!rel.startsWith('..' + path.sep) && rel !== '..' && !path.isAbsolute(rel))) return absPath;
  throw new Error(`Path is outside the KelMagic project root: ${absPath}`);
}
function resolveProjectPath(input, { mustExist = false } = {}) {
  if (typeof input !== 'string' || !input.trim()) throw new Error('A non-empty project-relative path is required.');
  const abs = path.isAbsolute(input) ? path.resolve(input) : path.resolve(ROOT, input);
  ensureUnderRoot(abs);
  if (mustExist && !fsStatSync(abs)) throw new Error(`File not found: ${abs}`);
  return abs;
}
function fsStatSync(p) {
  try { fsSync.statSync(p); return true; } catch { return false; }
}

function relativeToRoot(absPath) {
  ensureUnderRoot(absPath);
  return path.relative(ROOT, absPath).split(path.sep).join('/');
}
function safeSlug(value) {
  return String(value || 'asset').toLowerCase().replace(/[^a-z0-9_-]+/g, '_').replace(/^_+|_+$/g, '').slice(0, 64) || 'asset';
}
async function ensureDir(dir) { await fs.mkdir(dir, { recursive: true }); }
async function backupIfNeeded(destination, overwrite) {
  try {
    await fs.access(destination);
    if (!overwrite) throw new Error(`Destination already exists (nothing overwritten): ${relativeToRoot(destination)}. Set overwrite=true to replace it; a backup will be created.`);
    const rel = relativeToRoot(destination).replaceAll('/', '__');
    const backupDir = path.join(ROOT, 'art', 'approved', 'backups');
    await ensureDir(backupDir);
    const backupPath = path.join(backupDir, `${rel}.${new Date().toISOString().replaceAll(':', '-').replaceAll('.', '-')}.bak`);
    await fs.copyFile(destination, backupPath);
    return relativeToRoot(backupPath);
  } catch (err) {
    if (err?.code === 'ENOENT') return null;
    throw err;
  }
}
async function copyIntoProject(sourceAbs, targetRel, overwrite = false) {
  const targetAbs = resolveProjectPath(targetRel);
  const source = path.resolve(sourceAbs);
  ensureUnderRoot(source);
  const stat = await fs.stat(source);
  if (!stat.isFile()) throw new Error('Source must be a file.');
  await ensureDir(path.dirname(targetAbs));
  const backup = await backupIfNeeded(targetAbs, overwrite);
  await fs.copyFile(source, targetAbs);
  return { source: relativeToRoot(source), target: relativeToRoot(targetAbs), backup };
}
function baseUrl(pathname) {
  const base = new URL(config.comfyUIBaseUrl || DEFAULT_CONFIG.comfyUIBaseUrl);
  return new URL(pathname.replace(/^\//, ''), base.href.endsWith('/') ? base : `${base.href}/`);
}
async function fetchJson(url, init = {}, timeoutMs = 10000) {
  const resp = await fetch(url, { ...init, signal: init.signal || AbortSignal.timeout(timeoutMs) });
  const text = await resp.text();
  let body;
  try { body = text ? JSON.parse(text) : null; } catch { body = text; }
  if (!resp.ok) throw new Error(`HTTP ${resp.status} for ${url}: ${typeof body === 'string' ? body.slice(0, 1000) : JSON.stringify(body).slice(0, 1000)}`);
  return body;
}
function sortNodeIds(a, b) {
  const na = Number(a), nb = Number(b);
  if (Number.isFinite(na) && Number.isFinite(nb)) return na - nb;
  return String(a).localeCompare(String(b));
}
function uiWorkflowToApi(graph) {
  if (!Array.isArray(graph.nodes) || !Array.isArray(graph.links)) return structuredClone(graph);
  const links = new Map(graph.links.map((link) => [link[0], link]));
  const out = {};
  for (const node of graph.nodes) {
    const inputs = {};
    for (const input of node.inputs || []) {
      if (input.link !== null && input.link !== undefined) {
        const link = links.get(input.link);
        if (link) inputs[input.name] = [String(link[1]), link[2]];
      }
    }
    const w = Array.isArray(node.widgets_values) ? node.widgets_values : [];
    switch (node.type) {
      case 'CheckpointLoaderSimple': inputs.ckpt_name = w[0]; break;
      case 'CLIPTextEncode': inputs.text = w[0] ?? ''; break;
      case 'KSampler':
        inputs.seed = Number.isFinite(Number(w[0])) ? Number(w[0]) : Math.floor(Math.random() * 2147483647);
        inputs.steps = w.length >= 7 ? w[2] : w[1];
        inputs.cfg = w.length >= 7 ? w[3] : w[2];
        inputs.sampler_name = w.length >= 7 ? w[4] : w[3];
        inputs.scheduler = w.length >= 7 ? w[5] : w[4];
        inputs.denoise = w.length >= 7 ? w[6] : w[5];
        break;
      case 'EmptyLatentImage': inputs.width = w[0]; inputs.height = w[1]; inputs.batch_size = w[2] ?? 1; break;
      case 'SaveImage': inputs.filename_prefix = w[0] ?? 'KelMagic'; break;
      case 'VAEDecode': break;
      default:
        // For custom nodes, use known widget input names when possible. Unknown widgets remain untouched.
        for (const input of node.inputs || []) {
          if (input.widget?.name && input.link == null && !(input.name in inputs)) {
            const idx = (node.inputs || []).filter(x => x.widget?.name && x.link == null).findIndex(x => x.name === input.name);
            if (idx >= 0 && idx < w.length) inputs[input.name] = w[idx];
          }
        }
    }
    out[String(node.id)] = { class_type: node.type, inputs };
  }
  return out;
}
async function readWorkflow(workflowPath) {
  const defaultPath = path.join(HERE, 'workflows', 'KelMagic_Crystal_LCM.json');
  const abs = workflowPath ? resolveProjectPath(workflowPath, { mustExist: true }) : defaultPath;
  const data = JSON.parse(await fs.readFile(abs, 'utf8'));
  const api = uiWorkflowToApi(data);
  return { abs, data, api };
}
function listPromptNodes(api) {
  return Object.entries(api).sort(([a], [b]) => sortNodeIds(a, b)).filter(([_, n]) => {
    const type = String(n.class_type || '').toLowerCase();
    return (type.includes('textencode') || type.includes('cliptextencode')) && n.inputs && (typeof n.inputs.text === 'string' || typeof n.inputs.prompt === 'string');
  });
}
async function runProcess(exe, args, timeoutMs = 120000) {
  if (!exe || !path.isAbsolute(exe)) throw new Error('Application executable path is not configured. Run setup.ps1 and fill config.json.');
  if (!fsStatSync(exe)) throw new Error(`Executable not found: ${exe}`);
  return await new Promise((resolve, reject) => {
    const child = spawn(exe, args, { cwd: ROOT, windowsHide: false, stdio: ['ignore', 'pipe', 'pipe'] });
    let stdout = '', stderr = '', settled = false;
    const timer = setTimeout(() => {
      if (settled) return;
      settled = true;
      child.kill();
      reject(new Error(`Process timed out after ${timeoutMs / 1000}s. stderr: ${stderr.slice(-1500)}`));
    }, timeoutMs);
    child.stdout?.on('data', d => { stdout += d.toString(); });
    child.stderr?.on('data', d => { stderr += d.toString(); });
    child.on('error', err => { if (!settled) { settled = true; clearTimeout(timer); reject(err); } });
    child.on('close', code => {
      if (settled) return;
      settled = true; clearTimeout(timer);
      if (code !== 0) reject(new Error(`Process exited with code ${code}. stdout: ${stdout.slice(-1500)} stderr: ${stderr.slice(-1500)}`));
      else resolve({ code, stdout: stdout.slice(-5000), stderr: stderr.slice(-5000) });
    });
  });
}
function launchDetached(exe, args) {
  if (!exe || !path.isAbsolute(exe)) throw new Error('Application executable path is not configured. Run setup.ps1 and fill config.json.');
  if (!fsStatSync(exe)) throw new Error(`Executable not found: ${exe}`);
  const child = spawn(exe, args, { cwd: ROOT, detached: true, stdio: 'ignore', windowsHide: false });
  child.unref();
  return { launched: true, executable: exe, arguments: args };
}
function extractImages(historyEntry) {
  const images = [];
  for (const [nodeId, output] of Object.entries(historyEntry?.outputs || {})) {
    for (const img of output?.images || []) images.push({ nodeId, ...img });
  }
  return images;
}

const server = new McpServer({ name: 'kelmagic-studio-bridge', version: '0.1.0' });

addTool('kelmagic_status', 'Check the KelMagic project, ComfyUI API and configured creative applications.', {}, async () => {
  let comfy;
  try {
    const stats = await fetchJson(baseUrl('/system_stats'), {}, 4000);
    comfy = { online: true, url: config.comfyUIBaseUrl, devices: (stats.devices || []).map(d => ({ name: d.name, vramTotalGiB: Number(d.vram_total || 0) / 1073741824, vramFreeGiB: Number(d.vram_free || 0) / 1073741824 })) };
  } catch (e) { comfy = { online: false, url: config.comfyUIBaseUrl, error: e.message }; }
  const fileInfo = {};
  for (const rel of ['build.gradle', 'src/main/java/dev/kelmagic/KelMagic.java', 'art/generated', 'art/workflows/comfyui']) {
    const abs = path.join(ROOT, rel);
    fileInfo[rel] = fsStatSync(abs);
  }
  return { projectRoot: ROOT, files: fileInfo, comfyui: comfy, blockbenchExecutableConfigured: !!config.blockbenchExe && fsStatSync(config.blockbenchExe), pixeloramaExecutableConfigured: !!config.pixeloramaExe && fsStatSync(config.pixeloramaExe), blockbenchPluginSeen: !!latestBlockbenchReport, blockbenchReportAgeSeconds: latestBlockbenchReport ? Math.round((Date.now() - latestBlockbenchReport.receivedAt) / 1000) : null };
});

addTool('comfyui_status', 'Check ComfyUI /system_stats and queue.', {}, async () => {
  const stats = await fetchJson(baseUrl('/system_stats'));
  let queue = null;
  try { queue = await fetchJson(baseUrl('/queue')); } catch (e) { queue = { error: e.message }; }
  return { url: config.comfyUIBaseUrl, devices: stats.devices || [], queue };
});

addTool('comfyui_list_workflows', 'List available ComfyUI workflow JSON files from the project workflow folder and bundled templates.', {}, async () => {
  const dirs = [path.join(ROOT, 'art', 'workflows', 'comfyui'), path.join(HERE, 'workflows')];
  const list = [];
  for (const dir of dirs) {
    try {
      for (const entry of await fs.readdir(dir, { withFileTypes: true })) {
        if (entry.isFile() && entry.name.toLowerCase().endsWith('.json')) list.push({ path: path.relative(ROOT, path.join(dir, entry.name)).split(path.sep).join('/'), source: dir.startsWith(HERE) ? 'bundled template' : 'project workflow' });
      }
    } catch { /* directory is optional */ }
  }
  return list;
});

addTool('comfyui_inspect_workflow', 'Inspect a ComfyUI UI-format or API-format workflow and identify model and prompt nodes.', { workflowPath: z.string().optional() }, async ({ workflowPath }) => {
  const { abs, data, api } = await readWorkflow(workflowPath);
  return { path: relativeToRoot(abs), format: Array.isArray(data.nodes) ? 'ComfyUI editor workflow (bridge converts it to API format)' : 'ComfyUI API workflow', nodes: Object.entries(api).sort(([a], [b]) => sortNodeIds(a, b)).map(([id, node]) => ({ id, class_type: node.class_type, inputs: Object.fromEntries(Object.entries(node.inputs || {}).map(([k, v]) => [k, typeof v === 'string' && v.length > 180 ? v.slice(0, 180) + '…' : v])) })) };
});

addTool('comfyui_generate_asset', 'Run a local ComfyUI workflow, save generated images under art/generated, and optionally install the first image to a project-relative asset path. Existing assets are never overwritten unless overwrite=true (a backup is created).', {
  prompt: z.string().min(1),
  negativePrompt: z.string().optional(),
  assetName: z.string().optional(),
  workflowPath: z.string().optional(),
  modelName: z.string().optional(),
  width: z.number().int().min(64).max(2048).optional(),
  height: z.number().int().min(64).max(2048).optional(),
  targetPath: z.string().optional(),
  overwrite: z.boolean().optional(),
  timeoutSeconds: z.number().int().min(30).max(1800).optional()
}, async ({ prompt, negativePrompt, assetName, workflowPath, modelName, width, height, targetPath, overwrite = false, timeoutSeconds = 600 }) => {
  if (targetPath) {
    const target = resolveProjectPath(targetPath);
    if (fsStatSync(target) && !overwrite) throw new Error(`Target already exists; generation not started: ${targetPath}. Use overwrite=true to back it up and replace it.`);
  }
  const { api } = await readWorkflow(workflowPath);
  const graph = structuredClone(api);
  const texts = listPromptNodes(graph);
  if (!texts.length) throw new Error('No text-encode node with a text/prompt input found. Run comfyui_inspect_workflow and use a standard API-compatible workflow.');
  const positive = texts[0];
  const negative = texts[1];
  const positiveKey = typeof positive[1].inputs.text === 'string' ? 'text' : 'prompt';
  positive[1].inputs[positiveKey] = prompt;
  if (negativePrompt && negative) {
    const negativeKey = typeof negative[1].inputs.text === 'string' ? 'text' : 'prompt';
    negative[1].inputs[negativeKey] = negativePrompt;
  }
  for (const node of Object.values(graph)) {
    if (node.class_type === 'CheckpointLoaderSimple' && modelName) node.inputs.ckpt_name = modelName;
    if (node.class_type === 'EmptyLatentImage') {
      if (width) node.inputs.width = width;
      if (height) node.inputs.height = height;
    }
    if (node.class_type === 'KSampler') node.inputs.seed = Math.floor(Math.random() * 2147483647);
    if (node.class_type === 'SaveImage') node.inputs.filename_prefix = `KelMagic/${safeSlug(assetName || 'asset')}`;
  }
  const queued = await fetchJson(baseUrl('/prompt'), { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ prompt: graph, client_id: `kelmagic-${os.hostname()}` }) }, 15000);
  if (!queued?.prompt_id) throw new Error(`ComfyUI did not return prompt_id: ${JSON.stringify(queued)}`);
  const deadline = Date.now() + timeoutSeconds * 1000;
  let entry = null;
  while (Date.now() < deadline) {
    await new Promise(r => setTimeout(r, 1500));
    const history = await fetchJson(baseUrl(`/history/${encodeURIComponent(queued.prompt_id)}`), {}, 10000);
    entry = history?.[queued.prompt_id] || null;
    if (entry && extractImages(entry).length) break;
    if (entry?.status?.status_str === 'error') throw new Error(`ComfyUI execution error: ${JSON.stringify(entry.status.messages || entry.status)}`);
  }
  if (!entry) throw new Error(`Timed out waiting for ComfyUI prompt ${queued.prompt_id} after ${timeoutSeconds}s; it may still be queued/running.`);
  const images = extractImages(entry);
  if (!images.length) throw new Error(`ComfyUI finished but returned no images. History: ${JSON.stringify(entry.status || {})}`);
  const generatedDir = path.join(ROOT, 'art', 'generated');
  await ensureDir(generatedDir);
  const saved = [];
  const timestamp = new Date().toISOString().replace(/[-:]/g, '').replace(/\.\d+Z$/, 'Z');
  for (let i = 0; i < images.length; i++) {
    const img = images[i];
    const ext = path.extname(img.filename || '') || '.png';
    const filename = `${safeSlug(assetName || 'asset')}_${timestamp}${images.length > 1 ? `_${i + 1}` : ''}${ext}`;
    const view = new URL('/view', new URL(config.comfyUIBaseUrl));
    view.searchParams.set('filename', img.filename);
    view.searchParams.set('subfolder', img.subfolder || '');
    view.searchParams.set('type', img.type || 'output');
    const response = await fetch(view, { signal: AbortSignal.timeout(30000) });
    if (!response.ok) throw new Error(`Could not download ComfyUI image ${img.filename}: HTTP ${response.status}`);
    const imagePath = path.join(generatedDir, filename);
    await fs.writeFile(imagePath, Buffer.from(await response.arrayBuffer()));
    saved.push({ path: relativeToRoot(imagePath), absolutePath: imagePath, filename: img.filename, nodeId: img.nodeId });
  }
  let installed = null;
  if (targetPath) installed = await copyIntoProject(saved[0].absolutePath, targetPath, overwrite);
  return { promptId: queued.prompt_id, images: saved, installed, note: targetPath ? 'First generated image copied to requested project resource path.' : 'Images saved to art/generated; provide targetPath to install into resources automatically.' };
});

addTool('kelmagic_copy_asset', 'Copy a generated or approved file into the project. By default, existing files are not overwritten.', {
  sourcePath: z.string(), targetPath: z.string(), overwrite: z.boolean().optional()
}, async ({ sourcePath, targetPath, overwrite = false }) => {
  const source = resolveProjectPath(sourcePath, { mustExist: true });
  return await copyIntoProject(source, targetPath, overwrite);
});

addTool('kelmagic_list_artifacts', 'List generated artwork, approved artwork, Pixelorama projects and Blockbench models.', {}, async () => {
  const roots = ['art/generated', 'art/approved', 'art/pixelorama', 'art/blockbench'];
  const result = {};
  for (const rel of roots) {
    const abs = path.join(ROOT, rel);
    const files = [];
    async function walk(dir, depth = 0) {
      if (depth > 5) return;
      let entries;
      try { entries = await fs.readdir(dir, { withFileTypes: true }); } catch { return; }
      for (const e of entries) {
        const p = path.join(dir, e.name);
        if (e.isDirectory()) await walk(p, depth + 1);
        else files.push(relativeToRoot(p));
      }
    }
    await walk(abs);
    result[rel] = files.slice(0, 250);
  }
  return result;
});

addTool('pixelorama_open_file', 'Open a PNG or PXO project in Pixelorama.', { path: z.string() }, async ({ path: input }) => {
  const abs = resolveProjectPath(input, { mustExist: true });
  if (!/\.(png|pxo|apng|gif)$/i.test(abs)) throw new Error('Pixelorama open tool accepts .png, .pxo, .apng or .gif files.');
  return launchDetached(config.pixeloramaExe, [abs]);
});

addTool('pixelorama_inspect_project', 'Ask Pixelorama CLI for basic facts about an existing .pxo project (canvas size and frame count).', {
  projectPath: z.string(), metric: z.enum(['size', 'framecount']).optional()
}, async ({ projectPath, metric = 'size' }) => {
  const src = resolveProjectPath(projectPath, { mustExist: true });
  if (!src.toLowerCase().endsWith('.pxo')) throw new Error('projectPath must point to a Pixelorama .pxo project.');
  const flag = metric === 'framecount' ? '--framecount' : '--size';
  const proc = await runProcess(config.pixeloramaExe, ['--headless', '--quit', '--', flag, src], 120000);
  return { project: relativeToRoot(src), metric, stdout: proc.stdout, stderr: proc.stderr };
});

addTool('pixelorama_export_project', 'Export a Pixelorama .pxo project using its documented desktop CLI. The destination is project-relative and is not overwritten by default.', {
  projectPath: z.string(), outputPath: z.string(), scale: z.number().int().min(1).max(16).optional(), overwrite: z.boolean().optional()
}, async ({ projectPath, outputPath, scale = 1, overwrite = false }) => {
  const src = resolveProjectPath(projectPath, { mustExist: true });
  if (!src.toLowerCase().endsWith('.pxo')) throw new Error('projectPath must point to a Pixelorama .pxo project.');
  const dst = resolveProjectPath(outputPath);
  if (!/\.(png|apng|gif)$/i.test(dst)) throw new Error('outputPath must end in .png, .apng or .gif.');
  await ensureDir(path.dirname(dst));
  const backup = await backupIfNeeded(dst, overwrite);
  const args = ['--headless', '--quit', '--', '--export', '--output', dst, '--scale', String(scale), src];
  const proc = await runProcess(config.pixeloramaExe, args, 180000);
  if (!fsStatSync(dst)) throw new Error(`Pixelorama CLI returned successfully but output was not created: ${dst}. CLI output: ${proc.stdout} ${proc.stderr}`);
  return { project: relativeToRoot(src), output: relativeToRoot(dst), backup, stdout: proc.stdout, stderr: proc.stderr };
});


async function resolvePiskelCliEntry() {
  const packageDir = path.resolve(config.piskelCliPackageDir || DEFAULT_CONFIG.piskelCliPackageDir);
  const packageJsonPath = path.join(packageDir, 'package.json');
  if (!fsStatSync(packageJsonPath)) {
    throw new Error('Piskel CLI package not found at ' + packageDir + '. Expected the global @ne9roni/piskel-cli installation.');
  }
  let pkg;
  try { pkg = JSON.parse(await fs.readFile(packageJsonPath, 'utf8')); }
  catch (err) { throw new Error('Could not read Piskel CLI package.json: ' + err.message); }
  const bin = typeof pkg.bin === 'string' ? pkg.bin : pkg.bin?.['piskel-cli'];
  if (!bin) throw new Error('Installed Piskel CLI package.json has no piskel-cli bin entry.');
  const entry = path.resolve(packageDir, bin);
  if (!fsStatSync(entry)) throw new Error('Piskel CLI entry point not found: ' + entry);
  return { packageDir, entry, version: pkg.version || 'unknown' };
}

function resolvePiskelOutputPath(input, allowedFolder, extension) {
  if (typeof input !== 'string' || !input.trim()) throw new Error('A non-empty project-relative path is required.');
  if (path.isAbsolute(input)) throw new Error('Use a project-relative path, not an absolute path: ' + input);
  const abs = resolveProjectPath(input);
  const rel = relativeToRoot(abs);
  if (!rel.startsWith(allowedFolder + '/')) throw new Error('Output must be inside ' + allowedFolder + '/: ' + rel);
  if (path.extname(rel).toLowerCase() !== extension) throw new Error('Expected ' + extension + ' output: ' + rel);
  return { abs, rel };
}

const PISKEL_PLAN_COMMANDS = new Set([
  'project.create',
  'draw.pixel',
  'draw.line',
  'draw.rect',
  'draw.circle',
  'fill.area',
  'erase.pixel',
  'clear.frame',
  'export.png'
]);

addTool('piskel_cli_status', 'Check the installed Piskel CLI entry point without invoking PowerShell or shell scripts.', {}, async () => {
  const cli = await resolvePiskelCliEntry();
  const proc = await runProcess(process.execPath, [cli.entry, '--help'], 15000);
  return {
    available: true,
    version: cli.version,
    entry: cli.entry,
    packageDir: cli.packageDir,
    helpPreview: proc.stdout.slice(0, 1600),
    stderr: proc.stderr.slice(-1200)
  };
});

addTool('piskel_create_sprite', 'Create a pixel-art sprite from a Piskel CLI JSON plan, export and validate the PNG. Outputs are restricted to art/pixelorama and art/generated.', {
  plan: z.object({
    steps: z.array(z.object({ command: z.string(), args: z.record(z.any()).optional() }).passthrough()).min(3).max(300)
  }).passthrough(),
  projectPath: z.string().min(1),
  outputPath: z.string().min(1),
  overwrite: z.boolean().optional(),
  timeoutSeconds: z.number().int().min(5).max(180).optional()
}, async ({ plan, projectPath, outputPath, overwrite = false, timeoutSeconds = 60 }) => {
  const cli = await resolvePiskelCliEntry();
  const project = resolvePiskelOutputPath(projectPath, 'art/pixelorama', '.piskel');
  const output = resolvePiskelOutputPath(outputPath, 'art/generated', '.png');
  if (project.abs.toLowerCase() === output.abs.toLowerCase()) throw new Error('Project and PNG output paths must be different.');

  if (!plan || !Array.isArray(plan.steps) || plan.steps.length < 3 || plan.steps.length > 300) {
    throw new Error('plan.steps must contain between 3 and 300 steps.');
  }
  const normalized = structuredClone(plan);
  const commands = normalized.steps.map((step) => step?.command);
  const createIndexes = commands.map((v, i) => v === 'project.create' ? i : -1).filter(i => i >= 0);
  const exportIndexes = commands.map((v, i) => v === 'export.png' ? i : -1).filter(i => i >= 0);
  if (createIndexes.length !== 1 || createIndexes[0] !== 0) throw new Error('Plan must begin with exactly one project.create step.');
  if (exportIndexes.length !== 1 || exportIndexes[0] !== normalized.steps.length - 1) throw new Error('Plan must end with exactly one export.png step.');

  const createArgs = normalized.steps[0].args;
  if (!createArgs || typeof createArgs !== 'object' || Array.isArray(createArgs)) throw new Error('project.create requires an args object.');
  const width = Number(createArgs.width), height = Number(createArgs.height);
  if (!Number.isInteger(width) || width < 1 || width > 512 || !Number.isInteger(height) || height < 1 || height > 512) {
    throw new Error('Canvas width and height must be integers from 1 to 512.');
  }

  for (let i = 0; i < normalized.steps.length; i++) {
    const step = normalized.steps[i];
    if (!step || !PISKEL_PLAN_COMMANDS.has(step.command)) throw new Error('Unsupported Piskel plan command at step ' + i + ': ' + String(step?.command));
    if (!step.args || typeof step.args !== 'object' || Array.isArray(step.args)) throw new Error('Every Piskel plan step requires an args object (step ' + i + ').');
    if (step.command === 'project.create') {
      step.args.output = project.rel;
      step.args.name = path.basename(project.rel, '.piskel');
    } else {
      step.args.project = project.rel;
      if (Object.prototype.hasOwnProperty.call(step.args, 'output')) step.args.output = project.rel;
    }
    if (step.command === 'export.png') {
      step.args.project = project.rel;
      step.args.output = output.rel;
    }
  }

  const projectExists = fsStatSync(project.abs);
  const outputExists = fsStatSync(output.abs);
  if (!overwrite && (projectExists || outputExists)) {
    throw new Error('Destination already exists; nothing was run. projectExists=' + projectExists + ', outputExists=' + outputExists + '. Choose new names or pass overwrite=true for backups.');
  }
  const backups = {};
  if (overwrite) {
    backups.project = await backupIfNeeded(project.abs, true);
    backups.output = await backupIfNeeded(output.abs, true);
  }

  await ensureDir(path.dirname(project.abs));
  await ensureDir(path.dirname(output.abs));
  const tempPlanPath = path.join(os.tmpdir(), 'kelmagic-piskel-plan-' + process.pid + '-' + Date.now() + '.json');
  try {
    await fs.writeFile(tempPlanPath, JSON.stringify(normalized, null, 2), 'utf8');
    const proc = await runProcess(process.execPath, [cli.entry, 'run', tempPlanPath, '--json'], timeoutSeconds * 1000);
    let cliPayload;
    try { cliPayload = JSON.parse(proc.stdout.trim()); }
    catch { throw new Error('Piskel CLI did not return valid JSON. stdout=' + proc.stdout.slice(-2500) + ' stderr=' + proc.stderr.slice(-2500)); }
    if (cliPayload?.ok !== true || cliPayload?.data?.run?.status !== 'completed') {
      throw new Error('Piskel plan did not report success: ' + JSON.stringify(cliPayload).slice(0, 4000));
    }
    if (!fsStatSync(project.abs)) throw new Error('Piskel CLI reported success but did not create the .piskel project: ' + project.rel);
    if (!fsStatSync(output.abs)) throw new Error('Piskel CLI reported success but did not create the PNG: ' + output.rel);

    const png = await fs.readFile(output.abs);
    const signature = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]);
    if (png.length < 24 || !png.subarray(0, 8).equals(signature)) throw new Error('Output file does not have a valid PNG signature: ' + output.rel);
    const pngWidth = png.readUInt32BE(16), pngHeight = png.readUInt32BE(20);
    if (pngWidth < 1 || pngHeight < 1) throw new Error('PNG header contains invalid dimensions.');

    return {
      ok: true,
      cliVersion: cli.version,
      run: cliPayload.data.run,
      projectPath: project.rel,
      outputPath: output.rel,
      width: pngWidth,
      height: pngHeight,
      bytes: png.length,
      pngSignature: '89-50-4E-47-0D-0A-1A-0A',
      backups,
      stdout: proc.stdout.slice(-3000),
      stderr: proc.stderr.slice(-1500)
    };
  } finally {
    await fs.rm(tempPlanPath, { force: true }).catch(() => {});
  }
});

addTool('blockbench_status', 'Read the latest active-model report sent by the KelMagic Blockbench plugin.', {}, async () => {
  if (!latestBlockbenchReport) return { connected: false, message: 'No report received yet. Open Blockbench, load blockbench/kelmagic_studio_bridge.js as a local plugin, then use Tools > KelMagic: Send Model Status.' };
  const age = Math.round((Date.now() - latestBlockbenchReport.receivedAt) / 1000);
  return { connected: age <= 20, secondsSinceReport: age, ...latestBlockbenchReport.report };
});

addTool('blockbench_open_model', 'Open a .bbmodel project or a Minecraft Java model JSON in Blockbench.', { path: z.string() }, async ({ path: input }) => {
  const abs = resolveProjectPath(input, { mustExist: true });
  if (!/\.(bbmodel|json|obj|gltf|glb)$/i.test(abs)) throw new Error('Supported file types: .bbmodel, .json, .obj, .gltf, .glb.');
  return launchDetached(config.blockbenchExe, [abs]);
});

addTool('kelmagic_validate_resources', 'Validate KelMagic language JSON and item/block model JSON files, including referenced texture existence.', {}, async () => {
  const assetsRoot = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'kelmagic');
  const problems = [], checked = [];
  async function visit(dir, callback) {
    let entries;
    try { entries = await fs.readdir(dir, { withFileTypes: true }); } catch { return; }
    for (const e of entries) {
      const p = path.join(dir, e.name);
      if (e.isDirectory()) await visit(p, callback); else await callback(p);
    }
  }
  await visit(path.join(assetsRoot, 'lang'), async p => {
    if (!p.endsWith('.json')) return;
    try { JSON.parse(await fs.readFile(p, 'utf8')); checked.push(relativeToRoot(p)); }
    catch (e) { problems.push({ file: relativeToRoot(p), error: `Invalid JSON: ${e.message}` }); }
  });
  await visit(path.join(assetsRoot, 'models'), async p => {
    if (!p.endsWith('.json')) return;
    let model;
    try { model = JSON.parse(await fs.readFile(p, 'utf8')); checked.push(relativeToRoot(p)); }
    catch (e) { problems.push({ file: relativeToRoot(p), error: `Invalid JSON: ${e.message}` }); return; }
    const textures = model.textures || {};
    for (const textureRef of Object.values(textures)) {
      if (typeof textureRef !== 'string' || textureRef.startsWith('#')) continue;
      const [namespace, rest] = textureRef.includes(':') ? textureRef.split(':', 2) : ['minecraft', textureRef];
      if (namespace !== 'kelmagic') continue;
      const texturePath = path.join(ROOT, 'src', 'main', 'resources', 'assets', namespace, 'textures', `${rest}.png`);
      if (!fsStatSync(texturePath)) problems.push({ file: relativeToRoot(p), error: `Missing texture ${textureRef} at ${relativeToRoot(texturePath)}` });
    }
  });
  return { valid: problems.length === 0, checkedFiles: checked.length, checked, problems };
});

// A small loopback-only HTTP endpoint receives Blockbench plugin telemetry. MCP remains stdio.
const httpServer = http.createServer(async (req, res) => {
  const corsOrigin = req.headers.origin;
  if (!corsOrigin || corsOrigin === 'null' || corsOrigin.startsWith('file://') || corsOrigin.startsWith('blockbench://') || corsOrigin.startsWith('app://')) {
    res.setHeader('Access-Control-Allow-Origin', corsOrigin || 'null');
  }
  res.setHeader('Vary', 'Origin');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, X-KelMagic-Token');
  if (req.method === 'OPTIONS') { res.writeHead(204); res.end(); return; }
  const url = new URL(req.url || '/', `http://${config.bridgeHost}:${config.bridgePort}`);
  if (url.pathname === '/health' && req.method === 'GET') {
    res.writeHead(200, { 'Content-Type': 'application/json' }); res.end(JSON.stringify({ ok: true, service: 'kelmagic-studio-bridge' })); return;
  }
  if (url.pathname === '/blockbench/status' && req.method === 'POST') {
    if (req.headers['x-kelmagic-token'] !== config.blockbenchToken) { res.writeHead(403); res.end('forbidden'); return; }
    let body = '';
    req.on('data', chunk => { body += chunk; if (body.length > 1024 * 1024) req.destroy(); });
    req.on('end', () => {
      try {
        latestBlockbenchReport = { receivedAt: Date.now(), report: JSON.parse(body || '{}') };
        res.writeHead(200, { 'Content-Type': 'application/json' }); res.end(JSON.stringify({ ok: true, receivedAt: latestBlockbenchReport.receivedAt }));
      } catch { res.writeHead(400); res.end('invalid JSON'); }
    });
    return;
  }
  res.writeHead(404, { 'Content-Type': 'application/json' }); res.end(JSON.stringify({ error: 'not found' }));
});
httpServer.listen(Number(config.bridgePort) || 18765, config.bridgeHost || '127.0.0.1', () => {
  console.error(`[KelMagic Bridge] local Blockbench endpoint listening on http://${config.bridgeHost}:${config.bridgePort}`);
  console.error(`[KelMagic Bridge] project root: ${ROOT}`);
});

const transport = new StdioServerTransport();
await server.connect(transport);
