"""
Renders the ZeroFake interview and concepts guide to PDF.

    python docs/build_guide.py

Content lives in guide_content.py; this module is only presentation.
"""

import os
import re
import sys

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.platypus import (
    BaseDocTemplate,
    CondPageBreak,
    Frame,
    KeepTogether,
    NextPageTemplate,
    PageBreak,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
)

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from guide_content import CONTENT, DOC_SUBTITLE, DOC_TITLE  # noqa: E402
from guide_content_part2 import EXTRA  # noqa: E402

CONTENT = CONTENT + EXTRA

# ---------------------------------------------------------------------------
# Palette
# ---------------------------------------------------------------------------

INK = colors.HexColor("#1a1f2b")
MUTED = colors.HexColor("#5b6472")
ACCENT = colors.HexColor("#1e4d8c")
ACCENT_LIGHT = colors.HexColor("#e8eef7")
RULE = colors.HexColor("#c9d2e0")
CODE_BG = colors.HexColor("#f4f6f9")
CODE_INK = colors.HexColor("#22303f")
WARN_BG = colors.HexColor("#fdf3e3")
WARN_EDGE = colors.HexColor("#d99a2b")
TIP_BG = colors.HexColor("#eaf4ec")
TIP_EDGE = colors.HexColor("#3d8a52")
KEY_BG = colors.HexColor("#eceef6")
KEY_EDGE = colors.HexColor("#5a6bb0")

PAGE_W, PAGE_H = A4
MARGIN = 19 * mm

# ---------------------------------------------------------------------------
# Styles
# ---------------------------------------------------------------------------

_base = getSampleStyleSheet()

S = {
    "title": ParagraphStyle(
        "title", parent=_base["Title"], fontName="Helvetica-Bold",
        fontSize=30, leading=36, textColor=INK, alignment=TA_CENTER,
    ),
    "subtitle": ParagraphStyle(
        "subtitle", parent=_base["Normal"], fontName="Helvetica",
        fontSize=13, leading=19, textColor=MUTED, alignment=TA_CENTER,
    ),
    "part": ParagraphStyle(
        "part", parent=_base["Normal"], fontName="Helvetica-Bold",
        fontSize=22, leading=27, textColor=colors.white,
        spaceBefore=0, spaceAfter=0,
    ),
    "h1": ParagraphStyle(
        "h1", parent=_base["Normal"], fontName="Helvetica-Bold",
        fontSize=16, leading=20, textColor=ACCENT,
        spaceBefore=15, spaceAfter=7,
    ),
    "h2": ParagraphStyle(
        "h2", parent=_base["Normal"], fontName="Helvetica-Bold",
        fontSize=12, leading=15, textColor=INK,
        spaceBefore=11, spaceAfter=4,
    ),
    "body": ParagraphStyle(
        "body", parent=_base["Normal"], fontName="Helvetica",
        fontSize=9.6, leading=14.2, textColor=INK,
        alignment=TA_JUSTIFY, spaceAfter=6,
    ),
    "bullet": ParagraphStyle(
        "bullet", parent=_base["Normal"], fontName="Helvetica",
        fontSize=9.6, leading=14, textColor=INK,
        leftIndent=11, bulletIndent=2, spaceAfter=3.5,
    ),
    "code": ParagraphStyle(
        "code", parent=_base["Normal"], fontName="Courier",
        fontSize=7.9, leading=10.4, textColor=CODE_INK,
    ),
    "cell": ParagraphStyle(
        "cell", parent=_base["Normal"], fontName="Helvetica",
        fontSize=8.5, leading=11.6, textColor=INK,
    ),
    "cellhead": ParagraphStyle(
        "cellhead", parent=_base["Normal"], fontName="Helvetica-Bold",
        fontSize=8.5, leading=11.6, textColor=colors.white,
    ),
    "callout": ParagraphStyle(
        "callout", parent=_base["Normal"], fontName="Helvetica",
        fontSize=9.3, leading=13.4, textColor=INK, alignment=TA_JUSTIFY,
    ),
    "q": ParagraphStyle(
        "q", parent=_base["Normal"], fontName="Helvetica-Bold",
        fontSize=10, leading=14, textColor=ACCENT, spaceBefore=9, spaceAfter=3,
    ),
    "toc1": ParagraphStyle(
        "toc1", parent=_base["Normal"], fontName="Helvetica-Bold",
        fontSize=10, leading=16, textColor=INK, spaceBefore=7,
    ),
    "toc2": ParagraphStyle(
        "toc2", parent=_base["Normal"], fontName="Helvetica",
        fontSize=9.2, leading=13.6, textColor=MUTED, leftIndent=12,
    ),
}

# ---------------------------------------------------------------------------
# Inline markup:  **bold**   `code`   *italic*
# ---------------------------------------------------------------------------


# Entities are resolved to characters *before* XML escaping, otherwise the
# leading ampersand is escaped and the entity renders literally. Only
# characters present in WinAnsiEncoding are used, since the built-in Helvetica
# and Courier fonts cannot render anything outside it.
ENTITIES = [
    ("&mdash;", "—"),
    ("&ndash;", "–"),
    ("&middot;", "·"),
    ("&hellip;", "…"),
    ("&rarr;", "->"),
    ("&larr;", "<-"),
    ("&nbsp;", " "),
]

_AMP = ""


def markup(text):
    out = text
    for entity, char in ENTITIES:
        out = out.replace(entity, char)
    out = out.replace("&amp;", _AMP)

    out = out.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    out = out.replace(_AMP, "&amp;")
    out = re.sub(r"\*\*(.+?)\*\*", r"<b>\1</b>", out, flags=re.S)
    out = re.sub(
        r"`(.+?)`",
        r'<font face="Courier" size="8.6" color="#22303f">\1</font>',
        out, flags=re.S,
    )
    out = re.sub(r"(?<![\w*])\*(?!\s)(.+?)(?<!\s)\*(?![\w*])", r"<i>\1</i>", out, flags=re.S)
    return out


def esc_plain(text):
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


# ---------------------------------------------------------------------------
# Block builders
# ---------------------------------------------------------------------------

CONTENT_W = PAGE_W - 2 * MARGIN


def code_block(text):
    lines = text.strip("\n").rstrip().split("\n")
    body = "<br/>".join(esc_plain(ln).replace(" ", "&nbsp;") for ln in lines)
    para = Paragraph(body, S["code"])
    tbl = Table([[para]], colWidths=[CONTENT_W])
    tbl.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, -1), CODE_BG),
        ("BOX", (0, 0), (-1, -1), 0.6, RULE),
        ("LEFTPADDING", (0, 0), (-1, -1), 7),
        ("RIGHTPADDING", (0, 0), (-1, -1), 7),
        ("TOPPADDING", (0, 0), (-1, -1), 6),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
    ]))
    return [tbl, Spacer(1, 7)]


def callout_block(kind, title, text):
    bg, edge = {
        "warn": (WARN_BG, WARN_EDGE),
        "tip": (TIP_BG, TIP_EDGE),
        "key": (KEY_BG, KEY_EDGE),
    }[kind]

    label = {"warn": "GOTCHA", "tip": "INTERVIEW TIP", "key": "KEY IDEA"}[kind]
    heading = f"<b>{esc_plain(label)}</b>"
    if title:
        heading += f" &nbsp;&mdash;&nbsp; <b>{esc_plain(title)}</b>"

    inner = [Paragraph(heading, S["callout"]), Spacer(1, 3),
             Paragraph(markup(text), S["callout"])]

    tbl = Table([[inner]], colWidths=[CONTENT_W])
    tbl.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, -1), bg),
        ("LINEBEFORE", (0, 0), (0, -1), 3, edge),
        ("BOX", (0, 0), (-1, -1), 0.4, edge),
        ("LEFTPADDING", (0, 0), (-1, -1), 9),
        ("RIGHTPADDING", (0, 0), (-1, -1), 9),
        ("TOPPADDING", (0, 0), (-1, -1), 7),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
    ]))
    return [tbl, Spacer(1, 8)]


def table_block(rows, widths=None):
    header, *body = rows
    ncols = len(header)

    if widths:
        total = sum(widths)
        col_w = [CONTENT_W * w / total for w in widths]
    else:
        col_w = [CONTENT_W / ncols] * ncols

    data = [[Paragraph(markup(c), S["cellhead"]) for c in header]]
    for r in body:
        data.append([Paragraph(markup(c), S["cell"]) for c in r])

    tbl = Table(data, colWidths=col_w, repeatRows=1)
    style = [
        ("BACKGROUND", (0, 0), (-1, 0), ACCENT),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("GRID", (0, 0), (-1, -1), 0.4, RULE),
        ("LEFTPADDING", (0, 0), (-1, -1), 5),
        ("RIGHTPADDING", (0, 0), (-1, -1), 5),
        ("TOPPADDING", (0, 0), (-1, -1), 4),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
    ]
    for i in range(1, len(data)):
        if i % 2 == 0:
            style.append(("BACKGROUND", (0, i), (-1, i), ACCENT_LIGHT))
    tbl.setStyle(TableStyle(style))
    return [tbl, Spacer(1, 8)]


def part_banner(text):
    para = Paragraph(esc_plain(text), S["part"])
    tbl = Table([[para]], colWidths=[CONTENT_W])
    tbl.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, -1), ACCENT),
        ("LEFTPADDING", (0, 0), (-1, -1), 13),
        ("RIGHTPADDING", (0, 0), (-1, -1), 13),
        ("TOPPADDING", (0, 0), (-1, -1), 13),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 13),
    ]))
    return [PageBreak(), tbl, Spacer(1, 12)]


# ---------------------------------------------------------------------------
# Page furniture
# ---------------------------------------------------------------------------


def draw_frame(canvas, doc):
    canvas.saveState()

    canvas.setStrokeColor(RULE)
    canvas.setLineWidth(0.5)
    canvas.line(MARGIN, PAGE_H - MARGIN + 6, PAGE_W - MARGIN, PAGE_H - MARGIN + 6)
    canvas.line(MARGIN, MARGIN - 6, PAGE_W - MARGIN, MARGIN - 6)

    canvas.setFont("Helvetica", 7.5)
    canvas.setFillColor(MUTED)
    canvas.drawString(MARGIN, PAGE_H - MARGIN + 10, "ZeroFake " + chr(0x2014) + " Architecture, Workflow & Interview Guide")
    canvas.drawRightString(PAGE_W - MARGIN, MARGIN - 15, str(canvas.getPageNumber()))

    canvas.restoreState()


def draw_cover(canvas, doc):
    canvas.saveState()

    # Masthead band across the top.
    canvas.setFillColor(ACCENT)
    canvas.rect(0, PAGE_H - 46 * mm, PAGE_W, 46 * mm, stroke=0, fill=1)

    canvas.setFillColor(colors.white)
    canvas.setFont("Helvetica-Bold", 11)
    canvas.drawString(MARGIN, PAGE_H - 22 * mm, "PROJECT DOCUMENTATION")
    canvas.setFont("Helvetica", 9)
    canvas.drawString(MARGIN, PAGE_H - 29 * mm,
                      "Hyperledger Fabric  " + chr(0x00B7) +
                      "  Spring Boot Microservices  " + chr(0x00B7) + "  React")

    # Rule under the title block.
    canvas.setStrokeColor(ACCENT)
    canvas.setLineWidth(2)
    canvas.line(PAGE_W / 2 - 28 * mm, PAGE_H - 133 * mm,
                PAGE_W / 2 + 28 * mm, PAGE_H - 133 * mm)

    # Footer band.
    canvas.setFillColor(ACCENT)
    canvas.rect(0, 0, PAGE_W, 16 * mm, stroke=0, fill=1)
    canvas.setFillColor(colors.white)
    canvas.setFont("Helvetica", 8.5)
    canvas.drawCentredString(
        PAGE_W / 2, 6.4 * mm,
        "Read Part I first, then keep Parts V and VIII to hand.")

    canvas.restoreState()


# ---------------------------------------------------------------------------
# Build
# ---------------------------------------------------------------------------


def build(path):
    doc = BaseDocTemplate(
        path, pagesize=A4,
        leftMargin=MARGIN, rightMargin=MARGIN,
        topMargin=MARGIN, bottomMargin=MARGIN,
        title=DOC_TITLE, author="ZeroFake", subject=DOC_SUBTITLE,
    )

    frame = Frame(MARGIN, MARGIN, CONTENT_W, PAGE_H - 2 * MARGIN, id="main")
    doc.addPageTemplates([
        PageTemplate(id="cover", frames=[frame], onPage=draw_cover),
        PageTemplate(id="body", frames=[frame], onPage=draw_frame),
    ])

    story = []

    # --- cover -------------------------------------------------------------
    story.append(Spacer(1, 74 * mm))
    story.append(Paragraph(DOC_TITLE, S["title"]))
    story.append(Spacer(1, 9))
    story.append(Paragraph(
        "Blockchain-Based Fake Product Detection<br/>"
        "&amp; Supply Chain Verification Platform",
        S["subtitle"]))
    story.append(Spacer(1, 28))
    story.append(Paragraph(DOC_SUBTITLE, S["subtitle"]))
    story.append(NextPageTemplate("body"))

    # --- content -----------------------------------------------------------
    for block in CONTENT:
        kind = block[0]

        if kind == "part":
            story += part_banner(block[1])
        elif kind == "h1":
            story.append(CondPageBreak(46))
            story.append(Paragraph(markup(block[1]), S["h1"]))
        elif kind == "h2":
            story.append(CondPageBreak(34))
            story.append(Paragraph(markup(block[1]), S["h2"]))
        elif kind == "p":
            story.append(Paragraph(markup(block[1]), S["body"]))
        elif kind == "ul":
            for item in block[1]:
                story.append(Paragraph(markup(item), S["bullet"],
                                       bulletText=chr(0x2022)))
            story.append(Spacer(1, 5))
        elif kind == "ol":
            for i, item in enumerate(block[1], 1):
                story.append(Paragraph(markup(item), S["bullet"],
                                       bulletText=f"{i}."))
            story.append(Spacer(1, 5))
        elif kind == "code":
            story += code_block(block[1])
        elif kind == "table":
            widths = block[2] if len(block) > 2 else None
            story += table_block(block[1], widths)
        elif kind in ("warn", "tip", "key"):
            story += callout_block(kind, block[1], block[2])
        elif kind == "q":
            story.append(CondPageBreak(52))
            story.append(Paragraph(markup(block[1]), S["q"]))
        elif kind == "toc1":
            story.append(Paragraph(markup(block[1]), S["toc1"]))
        elif kind == "toc2":
            story.append(Paragraph(markup(block[1]), S["toc2"]))
        elif kind == "pagebreak":
            story.append(PageBreak())
        elif kind == "space":
            story.append(Spacer(1, block[1]))
        else:
            raise ValueError("unknown block: " + kind)

    doc.build(story)
    return path


if __name__ == "__main__":
    here = os.path.dirname(os.path.abspath(__file__))
    out = os.path.join(here, "ZeroFake-Interview-Guide.pdf")
    build(out)
    print("wrote", out, os.path.getsize(out), "bytes")
