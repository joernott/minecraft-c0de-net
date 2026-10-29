"""Reading and writing PNG files with the standard library only, copied from scripts/generate_textures.py of c0de."""

import struct
import zlib
from pathlib import Path

Pixel = tuple[int, int, int, int]
Image = list[list[Pixel]]


def write_png(path: Path, image: Image) -> None:
    """Writes an RGBA image as PNG."""
    height = len(image)
    width = len(image[0])
    raw = b"".join(b"\x00" + b"".join(bytes(pixel) for pixel in row) for row in image)

    def chunk(kind: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(png)


def read_png(path: Path) -> Image:
    """Reads a PNG without interlacing: RGBA, RGB, gray (with alpha) or with a palette, of any bit depth."""
    data = path.read_bytes()
    pos, idat, palette, alpha = 8, b"", [], b""
    width = height = depth = kind = 0
    while pos < len(data):
        length, chunk = struct.unpack(">I4s", data[pos:pos + 8])
        body = data[pos + 8:pos + 8 + length]
        if chunk == b"IHDR":
            width, height, depth, kind = struct.unpack(">IIBB", body[:10])
        elif chunk == b"PLTE":
            palette = [tuple(body[i:i + 3]) for i in range(0, len(body), 3)]
        elif chunk == b"tRNS":
            alpha = body
        elif chunk == b"IDAT":
            idat += body
        pos += 12 + length
    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[kind]
    bits = channels * depth
    stride = (width * bits + 7) // 8
    step = max(1, bits // 8)
    raw = zlib.decompress(idat)
    rows, previous, i = [], bytearray(stride), 0
    for _ in range(height):
        method, line = raw[i], bytearray(raw[i + 1:i + 1 + stride])
        i += 1 + stride
        for x in range(stride):
            a = line[x - step] if x >= step else 0
            b = previous[x]
            c = previous[x - step] if x >= step else 0
            if method == 1:
                line[x] = (line[x] + a) & 255
            elif method == 2:
                line[x] = (line[x] + b) & 255
            elif method == 3:
                line[x] = (line[x] + (a + b) // 2) & 255
            elif method == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                line[x] = (line[x] + (a if pa <= pb and pa <= pc else b if pb <= pc else c)) & 255
        previous = line
        samples = []
        if depth < 8:
            for byte in line:
                for shift in range(8 - depth, -1, -depth):
                    samples.append((byte >> shift) & ((1 << depth) - 1))
        else:
            size = depth // 8
            samples = [line[j] for j in range(0, len(line), size)]
        row = []
        for x in range(width):
            values = samples[x * channels:(x + 1) * channels]
            if kind == 3:
                index = values[0]
                r, g, b = palette[index]
                row.append((r, g, b, alpha[index] if index < len(alpha) else 255))
            elif kind == 0:
                v = values[0] * 255 // ((1 << depth) - 1)
                row.append((v, v, v, 255))
            elif kind == 4:
                row.append((values[0], values[0], values[0], values[1]))
            elif kind == 2:
                row.append((values[0], values[1], values[2], 255))
            else:
                row.append(tuple(values))
        rows.append(row)
    return rows
