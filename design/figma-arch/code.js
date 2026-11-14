// Seamstress — dev-плагин Figma: доска «экраны, переходы и состав».
// Запуск: Plugins → Development → Import plugin from manifest… → design/figma-arch/manifest.json
// Повторный запуск удаляет доску с префиксом «Seamstress · архитектура» и рисует заново.

const PREFIX = "Seamstress · архитектура";

const C = {
  canvas: "#F7F8FA",
  surface: "#FFFFFF",
  hairline: "#E5E9F0",
  ink: "#1B2430",
  muted: "#5B6875",
  faint: "#8A94A6",
  xml: "#6C5CE7",
  xmlBg: "#EEEBFF",
  compose: "#0A84FF",
  composeBg: "#E6F1FF",
  start: "#00B894",
  startBg: "#E1F7F0",
  edge: "#2F3E4E",
  back: "#8A94A6",
};

const FONT = {
  regular: { family: "Inter", style: "Regular" },
  medium: { family: "Inter", style: "Medium" },
  semibold: { family: "Inter", style: "Semi Bold" },
  bold: { family: "Inter", style: "Bold" },
};

const ROOT_W = 1440;
const ROOT_H = 1600;
const CARD_W = 320;
const GAP = 10;

const SHADOW_CARD = [{
  type: "DROP_SHADOW",
  color: { r: 0.07, g: 0.1, b: 0.14, a: 0.08 },
  offset: { x: 0, y: 2 },
  radius: 10,
  spread: 0,
  visible: true,
  blendMode: "NORMAL",
}];

function rgb(hex) {
  const h = hex.replace("#", "");
  return {
    r: parseInt(h.slice(0, 2), 16) / 255,
    g: parseInt(h.slice(2, 4), 16) / 255,
    b: parseInt(h.slice(4, 6), 16) / 255,
  };
}

function solid(hex) {
  return [{ type: "SOLID", color: rgb(hex) }];
}

function tx(str, o) {
  o = o || {};
  const size = o.size || 14;
  const t = figma.createText();
  t.name = str.length > 30 ? str.slice(0, 30) + "…" : str;
  t.fontName = FONT[o.weight || "regular"];
  t.fontSize = size;
  t.lineHeight = { unit: "PIXELS", value: Math.round(size * (o.lh || 1.4)) };
  t.fills = solid(o.color || C.ink);
  if (o.ls) t.letterSpacing = { unit: "PIXELS", value: o.ls };
  t.textAutoResize = o.width ? "HEIGHT" : "WIDTH_AND_HEIGHT";
  if (o.width) t.resize(o.width, Math.round(size * 1.4));
  t.characters = str;
  t.name = PREFIX + " · текст";
  return t;
}

function stack(mode, name, o) {
  o = o || {};
  const v = mode === "VERTICAL";
  const f = figma.createFrame();
  f.name = PREFIX + " · " + name;
  f.layoutMode = mode;
  f.primaryAxisSizingMode = (v ? o.h : o.w) !== undefined ? "FIXED" : "AUTO";
  f.counterAxisSizingMode = (v ? o.w : o.h) !== undefined ? "FIXED" : "AUTO";
  f.resize(o.w !== undefined ? o.w : 100, o.h !== undefined ? o.h : 100);
  f.itemSpacing = o.gap || 0;
  const pad = o.pad || 0;
  f.paddingLeft = o.padL !== undefined ? o.padL : pad;
  f.paddingRight = o.padR !== undefined ? o.padR : pad;
  f.paddingTop = o.padT !== undefined ? o.padT : pad;
  f.paddingBottom = o.padB !== undefined ? o.padB : pad;
  f.fills = o.fill ? solid(o.fill) : [];
  f.strokes = o.stroke ? solid(o.stroke) : [];
  if (o.stroke) f.strokeWeight = o.sw || 1;
  if (o.radius) f.cornerRadius = o.radius;
  f.clipsContent = !!o.clip;
  if (o.main) f.primaryAxisAlignItems = o.main;
  if (o.cross) f.counterAxisAlignItems = o.cross;
  if (o.shadow) f.effects = SHADOW_CARD;
  return f;
}

function vstack(name, o) {
  return stack("VERTICAL", name, o);
}

function hstack(name, o) {
  return stack("HORIZONTAL", name, o);
}

function rule(w) {
  const r = figma.createRectangle();
  r.name = PREFIX + " · линейка";
  r.resize(w, 1);
  r.fills = solid(C.hairline);
  return r;
}

function chip(label, fg, bg) {
  const c = hstack("Чип · " + label, {
    fill: bg,
    radius: 999,
    padL: 10,
    padR: 10,
    padT: 4,
    padB: 4,
    cross: "CENTER",
  });
  c.appendChild(tx(label, { size: 11, weight: "medium", color: fg, ls: 0.3 }));
  return c;
}

function techChip(tech) {
  return tech === "compose"
    ? chip("Compose", C.compose, C.composeBg)
    : chip("XML", C.xml, C.xmlBg);
}

function card(name, o) {
  o = o || {};
  return vstack(name, {
    w: o.w || CARD_W,
    h: o.h,
    fill: C.surface,
    radius: 18,
    stroke: o.stroke || C.hairline,
    sw: o.sw || 1.5,
    pad: 20,
    gap: GAP,
    shadow: true,
  });
}

function cardBody(parent, lines) {
  const body = vstack("Строки", { w: CARD_W - 40, gap: 6 });
  for (const l of lines) {
    body.appendChild(tx(l, { size: 12, color: C.muted, width: CARD_W - 40 }));
  }
  parent.appendChild(body);
}

function sectionTitle(text) {
  return tx(text, { size: 15, weight: "bold" });
}

/* ---------- экраны ---------- */

const SCREENS = [
  {
    key: "list",
    title: "CustomersListFragment",
    desc: "список клиентов",
    tech: "xml",
    start: true,
    badge: "СТАРТ",
    x: 60, y: 560, h: 270,
    lines: [
      "ViewModel: SeamstressViewModel",
      "getAllCustomers: LiveData",
      "RecyclerView (vertical) + FAB",
      "menu_customers_list",
      "открывается первой",
    ],
  },
  {
    key: "add",
    title: "AddCustomerFragment",
    desc: "новый клиент",
    tech: "xml",
    x: 560, y: 140, h: 240,
    lines: [
      "ViewModel: SeamstressViewModel",
      "Channel<Long> newCustomerId",
      "поля: имя, телефон, баланс",
      "Save блокируется до ответа БД",
      "ошибки в TextInputLayout",
    ],
  },
  {
    key: "card",
    title: "CustomerCardFragment",
    desc: "карточка клиента",
    tech: "xml",
    x: 560, y: 560, h: 270,
    lines: [
      "SeamstressViewModel",
      "+ MeasurementsViewModel",
      "две LiveData",
      "item_customer + RecyclerView(h)",
      "FAB + кнопка «замер»",
    ],
  },
  {
    key: "settings",
    title: "SettingsFragment",
    desc: "настройки",
    tech: "compose",
    x: 560, y: 1020, h: 190,
    lines: [
      "ViewModel: нет",
      "SettingsScreen(onMetricsClick)",
      "Column из SettingsItem",
    ],
  },
  {
    key: "update",
    title: "UpdateCustomerFragment",
    desc: "редактирование клиента",
    tech: "xml",
    x: 1060, y: 140, h: 215,
    lines: [
      "SeamstressViewModel",
      "customer?, customerId",
      "Save/Cancel → назад в карточку",
      "Delete → CustomersList",
    ],
  },
  {
    key: "addmeas",
    title: "AddMeasurementFragment",
    desc: "новый замер",
    tech: "compose",
    x: 1060, y: 520, h: 240,
    lines: [
      "MeasurementsViewModel",
      "saving: mutableStateOf",
      "OutlinedTextField ×2",
      "Cancel → navigateUp",
      "save → popBackStack",
    ],
  },
  {
    key: "meas",
    title: "CardMeasurementFragment",
    desc: "карточка замера",
    tech: "xml",
    x: 1060, y: 800, h: 215,
    lines: [
      "MeasurementsViewModel",
      "+ TakenMeasurementsViewModel",
      "TextView ×3 + RecyclerView",
      "переходов нет (лист)",
    ],
  },
  {
    key: "metrics",
    title: "MetricsReferenceFragment",
    desc: "справочник мерок",
    tech: "compose",
    x: 1060, y: 1020, h: 190,
    lines: [
      "MetricViewModel",
      "getAllMetrics → observeAsState",
      "LazyColumn Metric",
    ],
  },
];

/* ---------- переходы ---------- */

const EDGES = [
  {
    name: "список → новый клиент",
    pts: [[380, 600], [470, 600], [470, 240], [560, 240]],
    label: "FAB · новый клиент", lx: 482, ly: 430,
  },
  {
    name: "список → карточка",
    pts: [[380, 660], [560, 660]],
    label: "клик · *customerEntityId", lx: 392, ly: 648,
  },
  {
    name: "список → настройки",
    pts: [[380, 720], [470, 720], [470, 1080], [560, 1080]],
    label: "меню «Настройки»", lx: 482, ly: 900,
  },
  {
    name: "новый клиент → список (отмена)",
    pts: [[560, 200], [500, 200], [500, 120], [200, 120], [200, 560]],
    dash: true,
    label: "Cancel", lx: 400, ly: 140,
  },
  {
    name: "новый клиент → карточка",
    pts: [[720, 380], [720, 560]],
    label: "Save → insert, popUpTo", lx: 735, ly: 470,
  },
  {
    name: "карточка → редактирование",
    pts: [[880, 600], [960, 600], [960, 240], [1060, 240]],
    label: "FAB · *customerEntityId", lx: 972, ly: 430,
  },
  {
    name: "карточка → новый замер",
    pts: [[880, 680], [1060, 680]],
    label: "«замер» · *customerId", lx: 895, ly: 668,
  },
  {
    name: "карточка → карточка замера",
    pts: [[880, 760], [960, 760], [960, 860], [1060, 860]],
    label: "клик · *measurementId", lx: 895, ly: 745,
  },
  {
    name: "настройки → справочник",
    pts: [[880, 1095], [1060, 1095]],
    label: "Справочник мерок", lx: 895, ly: 1082,
  },
];

/* ---------- линии ---------- */

const HEADS = {
  up: { d: "M 0 12 L 12 12 L 6 0 Z", ox: -6, oy: 0 },
  right: { d: "M 0 0 L 0 12 L 12 6 Z", ox: -12, oy: -6 },
  down: { d: "M 0 0 L 12 0 L 6 12 Z", ox: -6, oy: -12 },
  left: { d: "M 12 0 L 12 12 L 0 6 Z", ox: 0, oy: -6 },
};

function polyline(pts, o) {
  o = o || {};
  let minX = pts[0][0];
  let minY = pts[0][1];
  for (let i = 1; i < pts.length; i++) {
    if (pts[i][0] < minX) minX = pts[i][0];
    if (pts[i][1] < minY) minY = pts[i][1];
  }
  const d = pts.map(function (p, i) {
    return (i === 0 ? "M " : "L ") + (p[0] - minX) + " " + (p[1] - minY);
  }).join(" ");
  const v = figma.createVector();
  v.name = PREFIX + " · " + (o.name || "связь");
  v.vectorPaths = [{ windingRule: "NONZERO", data: d }];
  v.fills = [];
  v.strokes = solid(o.color || C.edge);
  v.strokeWeight = 2;
  v.strokeCap = "ROUND";
  v.strokeJoin = "ROUND";
  if (o.dash) v.dashPattern = [6, 6];
  v.x = minX;
  v.y = minY;
  return v;
}

function arrowhead(tip, dir, color) {
  const h = HEADS[dir];
  const v = figma.createVector();
  v.name = PREFIX + " · стрелка";
  v.vectorPaths = [{ windingRule: "NONZERO", data: h.d }];
  v.fills = solid(color || C.edge);
  v.strokes = [];
  v.x = tip[0] + h.ox;
  v.y = tip[1] + h.oy;
  return v;
}

function direction(a, b) {
  if (Math.abs(b[0] - a[0]) >= Math.abs(b[1] - a[1])) {
    return b[0] > a[0] ? "right" : "left";
  }
  return b[1] > a[1] ? "down" : "up";
}

function shorten(pts, by) {
  const a = pts[pts.length - 2];
  const b = pts[pts.length - 1];
  const dx = b[0] - a[0];
  const dy = b[1] - a[1];
  const len = Math.sqrt(dx * dx + dy * dy) || 1;
  const k = Math.max(0, len - by) / len;
  const out = pts.slice(0, -1);
  out.push([a[0] + dx * k, a[1] + dy * k]);
  return out;
}

/* ---------- вспомогательные панели ---------- */

function flowCard() {
  const f = card("Поток вызова", { h: 250 });
  f.appendChild(sectionTitle("Поток вызова"));
  f.appendChild(rule(CARD_W - 40));
  cardBody(f, [
    "Fragment",
    "→ ViewModel (viewModelScope)",
    "→ Repository (suspend)",
    "→ DAO → Room",
    "→ LiveData / Channel→Flow",
    "→ observe(viewLifecycleOwner)",
  ]);
  return f;
}

function layersCard() {
  const f = card("Слои", { h: 250 });
  f.appendChild(sectionTitle("Слои"));
  f.appendChild(rule(CARD_W - 40));
  cardBody(f, [
    "ui/fragments — 8 экранов",
    "viewmodel — 4 AndroidViewModel",
    "data — 4 репозитория (1:1 к DAO)",
    "domain — Room-сущности и DAO",
    "DI нет: ViewModel сам открывает БД",
  ]);
  return f;
}

function legendCard() {
  const f = card("Как читать", {});
  f.appendChild(sectionTitle("Как читать"));
  f.appendChild(rule(CARD_W - 40));

  const rows = [
    ["xml", "ViewBinding, XML"],
    ["compose", "Compose в ComposeView"],
    ["start", "открывается первой"],
  ];
  for (const r of rows) {
    const row = hstack("Строка легенды", { w: CARD_W - 40, gap: 8, cross: "CENTER" });
    row.appendChild(r[0] === "start" ? chip("СТАРТ", C.start, C.startBg) : techChip(r[0]));
    row.appendChild(tx(r[1], { size: 12, color: C.muted }));
    f.appendChild(row);
  }

  const notes = vstack("Пояснения", { w: CARD_W - 40, gap: 6, padT: 4 });
  notes.appendChild(tx("сплошная стрелка — действие навигации", { size: 12, color: C.muted, width: CARD_W - 40 }));
  notes.appendChild(tx("пунктир — возврат или отмена", { size: 12, color: C.muted, width: CARD_W - 40 }));
  notes.appendChild(tx("*arg — аргумент Safe Args (Long)", { size: 12, color: C.muted, width: CARD_W - 40 }));
  f.appendChild(notes);
  return f;
}

function notesCard() {
  const f = card("Что не показано стрелками", { w: 820, h: 300 });
  f.appendChild(sectionTitle("Что не показано стрелками"));
  f.appendChild(rule(820 - 40));
  const rows = [
    "UpdateCustomer Save/Cancel → CustomerCard, popUpTo(customersList)",
    "UpdateCustomer Delete → CustomersList",
    "AddMeasurement сохраняет и делает popBackStack",
    "CardMeasurement — лист, только системный «назад»",
  ];
  for (const r of rows) {
    f.appendChild(tx(r, { size: 12, color: C.muted, width: 820 - 40 }));
  }
  f.appendChild(rule(820 - 40));
  f.appendChild(tx("Объявлено в navigation.xml, но не вызывается", { size: 13, weight: "semibold" }));
  const dead = [
    "action_customerCard_to_customersList",
    "action_addMeasurement_to_customerCard",
    "action_addMeasurement_to_cardMeasurement",
    "action_cardMeasurement_to_cardCustomer",
  ];
  for (const d of dead) {
    f.appendChild(tx(d, { size: 12, color: C.faint, width: 820 - 40 }));
  }
  return f;
}

/* ---------- сборка ---------- */

function screenCard(s) {
  const c = card("Экран · " + s.title, {
    h: s.h,
    stroke: s.start ? C.start : (s.tech === "compose" ? C.compose : C.xml),
    sw: s.start ? 2.5 : 1.5,
  });
  const head = hstack("Шапка", { w: CARD_W - 40, gap: 8, cross: "CENTER", main: "SPACE_BETWEEN" });
  head.appendChild(tx(s.title, { size: 15, weight: "bold", width: 200 }));
  head.appendChild(techChip(s.tech));
  c.appendChild(head);
  if (s.desc) {
    c.appendChild(tx(s.desc, { size: 13, color: C.faint, width: CARD_W - 40 }));
  }
  c.appendChild(rule(CARD_W - 40));
  cardBody(c, s.lines);
  if (s.badge) {
    const row = hstack("Бейдж", { w: CARD_W - 40, gap: 8, cross: "CENTER" });
    row.appendChild(chip(s.badge, C.start, C.startBg));
    c.appendChild(row);
  }
  return c;
}

async function main() {
  await figma.loadFontAsync(FONT.regular);
  await figma.loadFontAsync(FONT.medium);
  await figma.loadFontAsync(FONT.semibold);
  await figma.loadFontAsync(FONT.bold);

  const page = figma.currentPage;
  for (const n of page.children.slice(0)) {
    if (n.name.indexOf(PREFIX) === 0) n.remove();
  }

  const root = figma.createFrame();
  root.name = PREFIX + " · доска";
  root.resize(ROOT_W, ROOT_H);
  root.fills = solid(C.canvas);
  root.clipsContent = false;
  root.x = 0;
  root.y = 0;
  page.appendChild(root);

  const title = tx("Seamstress — экраны и переходы", { size: 40, weight: "bold" });
  title.x = 60;
  title.y = 30;
  root.appendChild(title);

  const subtitle = tx("8 экранов · 9 переходов · аргументы только Long · по факту на 2026-11-15", {
    size: 16,
    color: C.muted,
  });
  subtitle.x = 60;
  subtitle.y = 82;
  root.appendChild(subtitle);

  // левая колонка
  const flow = flowCard();
  flow.x = 60;
  flow.y = 170;
  root.appendChild(flow);

  const layers = layersCard();
  layers.x = 60;
  layers.y = 470;
  root.appendChild(layers);

  const legend = legendCard();
  legend.x = 60;
  legend.y = 770;
  root.appendChild(legend);

  // экраны
  for (const s of SCREENS) {
    const c = screenCard(s);
    c.x = s.x;
    c.y = s.y;
    root.appendChild(c);
  }

  // переходы
  for (const e of EDGES) {
    const tip = e.pts[e.pts.length - 1];
    const before = e.pts[e.pts.length - 2];
    const dir = direction(before, tip);
    const color = e.dash ? C.back : C.edge;
    root.appendChild(polyline(shorten(e.pts, 9), { name: e.name, color: color, dash: e.dash }));
    root.appendChild(arrowhead(tip, dir, color));
    const label = tx(e.label, { size: 12, color: C.muted, width: 260 });
    label.x = e.lx;
    label.y = e.ly;
    root.appendChild(label);
  }

  // заметки
  const notes = notesCard();
  notes.x = 560;
  notes.y = 1230;
  root.appendChild(notes);

  figma.viewport.scrollAndZoomIntoView([root]);
  figma.notify("Готово: доска архитектуры (8 экранов, 9 переходов)");
  figma.closePlugin();
}

main().catch((e) => {
  figma.notify("Ошибка плагина: " + e.message, { error: true, timeout: 10000 });
  figma.closePlugin();
});
