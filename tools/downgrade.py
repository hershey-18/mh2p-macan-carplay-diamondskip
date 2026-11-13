import struct
import sys


def read_u1(b, o):
    return b[o], o + 1


def read_u2(b, o):
    return struct.unpack_from(">H", b, o)[0], o + 2


def read_u4(b, o):
    return struct.unpack_from(">I", b, o)[0], o + 4


def parse_constant_pool(b, o):
    count, o = read_u2(b, o)
    utf8 = {}
    i = 1
    while i < count:
        tag, o = read_u1(b, o)
        if tag == 1:
            length, o = read_u2(b, o)
            utf8[i] = b[o:o + length].decode("utf-8", "replace")
            o += length
        elif tag in (3, 4):
            o += 4
        elif tag in (5, 6):
            o += 8
            i += 1
        elif tag in (7, 8):
            o += 2
        elif tag in (9, 10, 11, 12):
            o += 4
        elif tag in (15, 16, 18, 19, 20):
            raise ValueError("constant tag %d is not valid for class version 48" % tag)
        else:
            raise ValueError("unsupported constant tag %d" % tag)
        i += 1
    return o, utf8


def rewrite_attributes(b, o, utf8, out, in_code):
    count, o = read_u2(b, o)
    kept = []
    removed = 0
    for _ in range(count):
        name_idx, o2 = read_u2(b, o)
        length, o2 = read_u4(b, o2)
        body = b[o2:o2 + length]
        o = o2 + length
        name = utf8.get(name_idx, "")
        if (in_code and name == "StackMapTable") or name in ("Signature", "NestHost", "NestMembers", "MethodParameters"):
            removed += 1
            continue
        if name == "Code":
            body, r = rewrite_code(body, utf8)
            removed += r
        kept.append(struct.pack(">HI", name_idx, len(body)) + body)
    out.append(struct.pack(">H", len(kept)))
    out.extend(kept)
    return o, removed


def rewrite_code(body, utf8):
    o = 0
    max_stack, o = read_u2(body, o)
    max_locals, o = read_u2(body, o)
    code_len, o = read_u4(body, o)
    code = body[o:o + code_len]
    o += code_len
    ex_len, o = read_u2(body, o)
    ex = body[o:o + ex_len * 8]
    o += ex_len * 8
    out = [struct.pack(">HHI", max_stack, max_locals, code_len), code,
           struct.pack(">H", ex_len), ex]
    o, removed = rewrite_attributes(body, o, utf8, out, True)
    return b"".join(out), removed


def downgrade(path, major):
    b = open(path, "rb").read()
    magic, o = read_u4(b, 0)
    if magic != 0xCAFEBABE:
        raise ValueError("not a class file")
    o = 8
    o, utf8 = parse_constant_pool(b, o)
    out = [struct.pack(">IHH", 0xCAFEBABE, 0, major), b[8:o]]
    out.append(b[o:o + 6])
    o += 6
    icount, o = read_u2(b, o)
    out.append(struct.pack(">H", icount))
    out.append(b[o:o + icount * 2])
    o += icount * 2
    total_removed = 0
    for _ in range(2):
        mcount, o = read_u2(b, o)
        out.append(struct.pack(">H", mcount))
        for _ in range(mcount):
            out.append(b[o:o + 6])
            o += 6
            o, r = rewrite_attributes(b, o, utf8, out, False)
            total_removed += r
    o, r = rewrite_attributes(b, o, utf8, out, False)
    total_removed += r
    if o != len(b):
        raise ValueError("trailing bytes after class file")
    open(path, "wb").write(b"".join(out))
    return total_removed


if __name__ == "__main__":
    for p in sys.argv[1:]:
        n = downgrade(p, 48)
        print("%s: major=48, removed %d StackMapTable attributes" % (p, n))
