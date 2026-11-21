// Shared QA Test Harness for Edumy Web (Pure Node.js v24 fetch, zero external deps)
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
export const QA_ROOT = path.resolve(__dirname, '..');
export const EVIDENCE_DIR = path.join(QA_ROOT, 'evidence');
export const EXEC_LOG_CSV = path.join(QA_ROOT, 'EDU-TEL-W-001_Execution_Log.csv');
export const STATE_FILE = path.join(__dirname, 'qa_env_state.json');

export const BASE_API = process.env.QA_API_BASE || 'http://localhost:5001';
export const BASE_WEB = process.env.QA_WEB_BASE || 'http://localhost:3002';
export const BASE_MAILPIT = process.env.QA_MAILPIT_BASE || 'http://localhost:8026';
export const BASE_PROM = process.env.QA_PROM_BASE || 'http://localhost:9091';
export const BASE_GRAFANA = process.env.QA_GRAFANA_BASE || 'http://localhost:3003';
export const COMMIT_HASH = process.env.QA_COMMIT_HASH || 'Edumy-main@836460a';
export const CURRENT_CYCLE = process.env.QA_CYCLE || 'C1';

// Test credentials kept strictly in memory via env or ephemeral test-only default, NEVER written to docs/evidence
export const QA_PASS = process.env.QA_TEST_PASSWORD || 'QaTest@2026!';
export const SEED_ADMIN_PASS = process.env.QA_SEED_ADMIN_PASS || 'Admin@123';
export const SEPAY_API_KEY = process.env.QA_SEPAY_KEY || 'edumy_sepay_webhook_secret_key_2026';

fs.mkdirSync(EVIDENCE_DIR, { recursive: true });

export function nowHcmIso() {
  const d = new Date();
  // Format in Asia/Ho_Chi_Minh (+07:00)
  const parts = new Intl.DateTimeFormat('sv-SE', {
    timeZone: 'Asia/Ho_Chi_Minh',
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
    hour12: false
  }).formatToParts(d);
  const m = Object.fromEntries(parts.map(p => [p.type, p.value]));
  return `${m.year}-${m.month}-${m.day}T${m.hour}:${m.minute}:${m.second}+07:00`;
}

export function sanitizeSecrets(obj) {
  const secretKeys = /password|token|secret|apikey|authorization|cookie|hash|resettoken|accountnumber/i;
  if (obj === null || obj === undefined) return obj;
  if (typeof obj === 'string') {
    let s = obj
      .replaceAll(QA_PASS, '***')
      .replaceAll(SEED_ADMIN_PASS, '***')
      .replaceAll(SEPAY_API_KEY, '***')
      .replace(/Bearer\s+[A-Za-z0-9\-_.]+/gi, 'Bearer ***')
      .replace(/Apikey\s+[^\s"']+/gi, 'Apikey ***')
      .replace(/eyJ[A-Za-z0-9\-_]+\.eyJ[A-Za-z0-9\-_]+\.[A-Za-z0-9\-_]+/g, '***JWT***');
    return s;
  }
  if (Array.isArray(obj)) return obj.map(sanitizeSecrets);
  if (typeof obj === 'object') {
    const out = {};
    for (const [k, v] of Object.entries(obj)) {
      if (secretKeys.test(k) && typeof v === 'string' && v.length > 0) {
        out[k] = '***';
      } else {
        out[k] = sanitizeSecrets(v);
      }
    }
    return out;
  }
  return obj;
}

export async function apiReq(method, urlPath, { token, cookie, headers = {}, body, rawBody, isFormData = false } = {}) {
  const url = urlPath.startsWith('http') ? urlPath : `${BASE_API}${urlPath}`;
  const reqHeaders = { ...headers };
  if (token) reqHeaders['Authorization'] = `Bearer ${token}`;
  if (cookie) reqHeaders['Cookie'] = cookie;
  let fetchBody;
  if (isFormData) {
    fetchBody = body;
  } else if (rawBody !== undefined) {
    fetchBody = rawBody;
    if (!reqHeaders['Content-Type']) reqHeaders['Content-Type'] = 'application/json';
  } else if (body !== undefined) {
    fetchBody = JSON.stringify(body);
    if (!reqHeaders['Content-Type']) reqHeaders['Content-Type'] = 'application/json';
  }

  const t0 = performance.now();
  const res = await fetch(url, { method, headers: reqHeaders, body: fetchBody, redirect: 'manual' });
  const elapsedMs = Math.round((performance.now() - t0) * 10) / 10;

  const setCookies = typeof res.headers.getSetCookie === 'function' ? res.headers.getSetCookie() : [];
  const ct = res.headers.get('content-type') || '';
  let data = null;
  let rawText = '';
  let byteLength = 0;

  if (ct.includes('application/pdf') || ct.includes('application/octet-stream') || ct.includes('image/')) {
    const buf = Buffer.from(await res.arrayBuffer());
    byteLength = buf.length;
    data = { _binary: true, contentType: ct, byteLength, headerAscii: buf.subarray(0, 12).toString('latin1') };
    rawText = `<binary ${ct} ${byteLength} bytes>`;
  } else {
    rawText = await res.text();
    byteLength = Buffer.byteLength(rawText, 'utf8');
    try {
      data = rawText ? JSON.parse(rawText) : null;
    } catch {
      data = rawText;
    }
  }

  // Unwrap ApiResponse envelope conveniently while keeping full raw body
  const unwrapped = (data && typeof data === 'object' && !Array.isArray(data) && 'success' in data && 'data' in data)
    ? data.data
    : data;

  return {
    status: res.status,
    ok: res.ok,
    elapsedMs,
    contentType: ct,
    byteLength,
    headers: Object.fromEntries(res.headers.entries()),
    setCookies,
    raw: data,
    rawText,
    data: unwrapped,
  };
}

export async function loginWeb(email, password = QA_PASS) {
  const res = await apiReq('POST', '/api/v1/auth/login', { body: { email, password } });
  const token = res.data?.token || res.data?.accessToken || null;
  const cookieHeader = (res.setCookies || []).map(c => c.split(';')[0]).join('; ');
  return { ...res, token, cookieHeader };
}

export async function mailpitList() {
  const res = await apiReq('GET', `${BASE_MAILPIT}/api/v1/messages?limit=100`);
  return res.raw?.messages || [];
}

export async function mailpitGet(id) {
  const res = await apiReq('GET', `${BASE_MAILPIT}/api/v1/message/${id}`);
  return res.raw;
}

export async function mailpitClear() {
  await apiReq('DELETE', `${BASE_MAILPIT}/api/v1/messages`);
}

export function saveEvidence(runId, slug, payload) {
  const safeSlug = slug.replace(/[^a-zA-Z0-9_-]/g, '_');
  const fileName = `${runId}_${safeSlug}.json`;
  const fullPath = path.join(EVIDENCE_DIR, fileName);
  const content = sanitizeSecrets(payload);
  fs.writeFileSync(fullPath, JSON.stringify(content, null, 2), 'utf8');
  return `evidence/${fileName}`;
}

export function csvEscape(val) {
  const s = String(val ?? '');
  if (s.includes(',') || s.includes('"') || s.includes('\n') || s.includes('\r')) {
    return `"${s.replaceAll('"', '""')}"`;
  }
  return s;
}

export function appendExecutionLog(entry) {
  const row = [
    entry.runId,
    entry.cycle || 'C1',
    entry.tcId,
    entry.module,
    entry.type,
    entry.priority,
    entry.status,
    sanitizeSecrets(entry.actual || ''),
    entry.defectId || '',
    entry.evidence || '',
    entry.commitHash || COMMIT_HASH,
    entry.tool || 'Node.js v24.16.0 fetch',
    entry.timestamp || nowHcmIso(),
    entry.executor || 'QA Lead',
    sanitizeSecrets(entry.note || ''),
  ].map(csvEscape).join(',');
  fs.appendFileSync(EXEC_LOG_CSV, row + '\n', 'utf8');
}

export function loadState() {
  return JSON.parse(fs.readFileSync(STATE_FILE, 'utf8'));
}

export function saveState(state) {
  fs.writeFileSync(STATE_FILE, JSON.stringify(sanitizeSecrets(state), null, 2), 'utf8');
}
