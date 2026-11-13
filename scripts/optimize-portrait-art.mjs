// Build-time PNG compression only; the Windows application needs no Node runtime.
import { readFileSync, writeFileSync, readdirSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { inflateSync, deflateSync } from 'node:zlib';

const art = resolve(dirname(fileURLToPath(import.meta.url)), '../windows/art');
const paeth = (a, b, c) => {
  const p = a + b - c, x = Math.abs(p - a), y = Math.abs(p - b), z = Math.abs(p - c);
  return x <= y && x <= z ? a : y <= z ? b : c;
};
function decode(data, width, height) {
  const stride = width * 4, result = Buffer.alloc(stride * height);
  if (data.length !== (stride + 1) * height) throw new Error('Unexpected PNG scanline size');
  for (let y = 0; y < height; y++) {
    const filter = data[y * (stride + 1)];
    if (filter > 4) throw new Error('Unknown PNG filter');
    for (let x = 0; x < stride; x++) {
      const i = y * stride + x, a = x >= 4 ? result[i - 4] : 0;
      const b = y ? result[i - stride] : 0, c = y && x >= 4 ? result[i - stride - 4] : 0;
      const predictor = [0, a, b, (a + b) >>> 1, paeth(a, b, c)][filter];
      result[i] = (data[y * (stride + 1) + 1 + x] + predictor) & 255;
    }
  }
  return result;
}
const crcTable = Array.from({ length: 256 }, (_, n) => {
  for (let bit = 0; bit < 8; bit++) n = n & 1 ? 0xedb88320 ^ (n >>> 1) : n >>> 1;
  return n >>> 0;
});
function chunk(type, value) {
  const payload = Buffer.concat([Buffer.from(type), value]), out = Buffer.alloc(value.length + 12);
  out.writeUInt32BE(value.length); payload.copy(out, 4);
  let crc = 0xffffffff;
  for (const byte of payload) crc = crcTable[(crc ^ byte) & 255] ^ (crc >>> 8);
  out.writeUInt32BE((crc ^ 0xffffffff) >>> 0, out.length - 4);
  return out;
}
let before = 0, after = 0;
const supported = /^(portrait_.*|madara_(cast|guardian|eye)|sukuna_(cast|shrine|eye)|zoro_(ready|combo|finish|slashes))\.png$/;
const selected = process.argv.slice(2);
if (selected.some(f => !supported.test(f))) throw new Error('Expected compiled portrait or Madara PNG filenames');
for (const file of selected.length ? selected : readdirSync(art).filter(f => supported.test(f))) {
  const path = resolve(art, file), source = readFileSync(path), chunks = [], compressed = [];
  for (let offset = 8; offset < source.length;) {
    const length = source.readUInt32BE(offset), type = source.toString('ascii', offset + 4, offset + 8);
    const data = source.subarray(offset + 8, offset + 8 + length);
    if (type === 'IDAT') compressed.push(data); else chunks.push({ type, data });
    offset += length + 12;
  }
  const header = chunks.find(c => c.type === 'IHDR').data;
  if (header[8] !== 8 || header[9] !== 6 || header[10] || header[11] || header[12]) throw new Error('Expected non-interlaced RGBA8 PNG');
  const width = header.readUInt32BE(0), height = header.readUInt32BE(4), stride = width * 4;
  const raw = decode(inflateSync(Buffer.concat(compressed)), width, height);
  // RGB under fully transparent pixels is invisible. Keep every visible RGBA sample unchanged.
  for (let i = 0; i < raw.length; i += 4) if (raw[i + 3] === 0) raw.fill(0, i, i + 3);
  const filtered = Buffer.alloc((stride + 1) * height);
  for (let y = 0; y < height; y++) {
    let best, bestScore = Infinity, bestFilter = 0;
    for (let filter = 0; filter <= 4; filter++) {
      const row = Buffer.alloc(stride); let score = 0;
      for (let x = 0; x < stride; x++) {
        const i = y * stride + x, a = x >= 4 ? raw[i - 4] : 0, b = y ? raw[i - stride] : 0;
        const c = y && x >= 4 ? raw[i - stride - 4] : 0;
        const predictor = [0, a, b, (a + b) >>> 1, paeth(a, b, c)][filter];
        row[x] = (raw[i] - predictor) & 255; score += Math.min(row[x], 256 - row[x]);
      }
      if (score < bestScore) { best = row; bestScore = score; bestFilter = filter; }
    }
    filtered[y * (stride + 1)] = bestFilter; best.copy(filtered, y * (stride + 1) + 1);
  }
  const encoded = deflateSync(filtered, { level: 9 });
  if (!decode(inflateSync(encoded), width, height).equals(raw)) throw new Error('PNG round-trip changed pixels');
  const output = Buffer.concat([source.subarray(0, 8), ...chunks.filter(c => c.type !== 'IEND').map(c => chunk(c.type, c.data)), chunk('IDAT', encoded), chunk('IEND', Buffer.alloc(0))]);
  if (output.length < source.length) writeFileSync(path, output);
  before += source.length; after += Math.min(output.length, source.length);
}
console.log(JSON.stringify({ beforeBytes: before, afterBytes: after, visiblePixelsUnchanged: true }));
