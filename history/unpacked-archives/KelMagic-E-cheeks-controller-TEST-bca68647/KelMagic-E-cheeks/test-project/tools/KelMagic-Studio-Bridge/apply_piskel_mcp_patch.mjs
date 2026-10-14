import fs from 'node:fs/promises';
import path from 'node:path';
import os from 'node:os';
import { spawnSync } from 'node:child_process';

const serverPath = path.resolve('server.js');
const source = await fs.readFile(serverPath, 'utf8');
if (source.includes("addTool('piskel_create_sprite'")) {
  console.error('ABORTED: piskel_create_sprite is already registered. No files changed. Review server.js manually first.');
  process.exit(2);
}
if (!source.includes("pixeloramaExe: '',")) {
  console.error("ABORTED: couldn't find DEFAULT_CONFIG pixeloramaExe anchor. No files changed.");
  process.exit(2);
}
if (!source.includes("addTool('blockbench_status'")) {
  console.error("ABORTED: couldn't find blockbench_status tool anchor. No files changed.");
  process.exit(2);
}

let updated = source;
updated = updated.replace(
  "pixeloramaExe: '',",
  "pixeloramaExe: '',\n  piskelCliPackageDir: path.join(process.env.APPDATA || path.join(os.homedir(), 'AppData', 'Roaming'), 'npm', 'node_modules', '@ne9roni', 'piskel-cli'),"
);

// Remove the attempted per-step implementation from the interrupted edit. The supported
// Piskel CLI contract is one invocation: `piskel-cli run <plan.json> --json`.
const runPlanStart = updated.indexOf('async function runPlan(plan) {');
const endpointAnchor = '// A small loopback-only HTTP endpoint receives Blockbench plugin telemetry. MCP remains stdio.';
if (runPlanStart >= 0) {
  const endpointIndex = updated.indexOf(endpointAnchor, runPlanStart);
  if (endpointIndex < 0) {
    console.error("ABORTED: found runPlan but couldn't find endpoint anchor after it. No files changed.");
    process.exit(2);
  }
  updated = updated.slice(0, runPlanStart) + updated.slice(endpointIndex);
}

const integration = String.raw`
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

`;
const anchor = "addTool('blockbench_status'";
const anchorIndex = updated.indexOf(anchor);
updated = updated.slice(0, anchorIndex) + integration + updated.slice(anchorIndex);

const backupPath = serverPath + '.before-piskel-' + new Date().toISOString().replaceAll(':', '-').replaceAll('.', '-') + '.bak';
await fs.copyFile(serverPath, backupPath);
await fs.writeFile(serverPath, updated, 'utf8');
const check = spawnSync(process.execPath, ['--check', serverPath], { encoding: 'utf8', windowsHide: true });
if (check.status !== 0) {
  await fs.copyFile(backupPath, serverPath);
  console.error('PATCH FAILED: node --check failed; original server.js has been restored.');
  console.error(check.stdout || '');
  console.error(check.stderr || '');
  process.exit(check.status || 1);
}
console.log('PATCH APPLIED and syntax checked successfully.');
console.log('Backup: ' + backupPath);
console.log('Added tools: piskel_cli_status, piskel_create_sprite');
console.log('Removed the interrupted/broken runPlan helper.');
console.log('No npm packages were added or changed.');
