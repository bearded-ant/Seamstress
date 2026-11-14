// Seamstress — dev-плагин Figma: рисует 6 экранов приложения редактируемыми слоями.
// Запуск: Plugins → Development → Import plugin from manifest… → design/figma-screens/manifest.json

const COLORS = {
  primary: "#6200EE",
  primaryDark: "#3700B3",
  bg: "#F3F1F6",
  surface: "#FFFFFF",
  hairline: "#E6E3EA",
  onSurface: "#1C1B1F",
  onSurfaceVariant: "#6E6A75",
  outline: "#C7C2CE",
  photo: "#ECEAF0",
  icon: "#B3AEBA",
  onPrimary: "#FFFFFF",
  error: "#B3261E",
};

const FONT = {
  regular: { family: "Inter", style: "Regular" },
  medium: { family: "Inter", style: "Medium" },
  semibold: { family: "Inter", style: "Semi Bold" },
  bold: { family: "Inter", style: "Bold" },
};

const W = 360;
const H = 800;
const APPBAR = 56;
const GAP = 80;
const CARD_W = W - 16;
const PREFIX = "Seamstress";

const SHADOW_CARD = [{
  type: "DROP_SHADOW",
  color: { r: 0, g: 0, b: 0, a: 0.06 },
  offset: { x: 0, y: 1 },
  radius: 4,
  spread: 0,
  visible: true,
  blendMode: "NORMAL",
}];

const SHADOW_FAB = [{
  type: "DROP_SHADOW",
  color: { r: 0, g: 0, b: 0, a: 0.3 },
  offset: { x: 0, y: 4 },
  radius: 8,
  spread: 0,
  visible: true,
  blendMode: "NORMAL",
}];

const ICON_ADD = '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path fill="#FFFFFF" d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>';
const ICON_BACK = '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path fill="#FFFFFF" d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg>';
const ICON_EDIT = '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path fill="#FFFFFF" d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a.996.996 0 0 0 0-1.41l-2.34-2.34a.996.996 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>';
const ICON_PHOTO = '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24"><path fill="#B3AEBA" d="M3 4V1h2v3h3v2H5v3H3V6H0V4h3zm3 6V7h3V4h7l1.83 2H21c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H5c-1.1 0-2-.9-2-2V10h3zm7 9c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-3.2-5c0 1.77 1.43 3.2 3.2 3.2s3.2-1.43 3.2-3.2-1.43-3.2-3.2-3.2-3.2 1.43-3.2 3.2z"/></svg>';
const ICON_PERSON = '<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24"><path fill="#B3AEBA" d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>';

function rgb(hex) {
  const h = hex.replace("#", "");
  return {
    r: parseInt(h.slice(0, 2), 16) / 255,
    g: parseInt(h.slice(2, 4), 16) / 255,
    b: parseInt(h.slice(4, 6), 16) / 255,
  };
}

function solid(hex, opacity) {
  return [{ type: "SOLID", color: rgb(hex), opacity: opacity === undefined ? 1 : opacity }];
}

function tx(str, o) {
  o = o || {};
  const size = o.size || 14;
  const t = figma.createText();
  t.name = str.length > 26 ? str.slice(0, 26) + "…" : str;
  t.fontName = FONT[o.weight || "regular"];
  t.fontSize = size;
  t.lineHeight = { unit: "PIXELS", value: Math.round(size * (o.lh || 1.35)) };
  t.fills = solid(o.color || COLORS.onSurface);
  if (o.ls) t.letterSpacing = { unit: "PIXELS", value: o.ls };
  t.textAutoResize = o.width ? "HEIGHT" : "WIDTH_AND_HEIGHT";
  if (o.width) t.resize(o.width, Math.round(size * 1.35));
  t.characters = str;
  if (o.align) t.textAlignHorizontal = o.align;
  return t;
}

function iconNode(svg, name) {
  const n = figma.createNodeFromSvg(svg);
  n.name = name || "icon";
  return n;
}

function stack(mode, name, o) {
  o = o || {};
  const v = mode === "VERTICAL";
  const f = figma.createFrame();
  f.name = name;
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
  if (o.stroke) f.strokeWeight = 1;
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

function appBar(title, withBack) {
  const bar = hstack("AppBar", {
    w: W,
    h: APPBAR,
    fill: COLORS.primary,
    padL: 16,
    padR: 16,
    gap: 20,
    cross: "CENTER",
  });
  if (withBack) bar.appendChild(iconNode(ICON_BACK, "back"));
  bar.appendChild(tx(title, { size: 20, weight: "medium", color: COLORS.onPrimary }));
  return bar;
}

function body() {
  return vstack("Body", { w: W, h: H - APPBAR, fill: COLORS.bg, pad: 8, gap: 8 });
}

function screen(name) {
  const f = figma.createFrame();
  f.name = PREFIX + " · " + name;
  f.resize(W, H);
  f.fills = solid(COLORS.surface);
  f.clipsContent = true;
  return f;
}

function photo(w, h, svg, name) {
  const f = hstack(name || "Фото", { w: w, h: h, fill: COLORS.photo, main: "CENTER", cross: "CENTER" });
  f.appendChild(iconNode(svg));
  return f;
}

function textButton(label, color) {
  const b = hstack("Кнопка · " + label, {
    fill: COLORS.surface,
    radius: 4,
    padL: 12,
    padR: 12,
    padT: 8,
    padB: 8,
    gap: 4,
    cross: "CENTER",
  });
  b.appendChild(tx(label, { size: 14, weight: "medium", color: color, ls: 0.8 }));
  return b;
}

function fab(svg, name) {
  const f = hstack(name || "FAB", {
    w: 56,
    h: 56,
    fill: COLORS.primary,
    radius: 28,
    main: "CENTER",
    cross: "CENTER",
  });
  f.appendChild(iconNode(svg, "fab-icon"));
  f.effects = SHADOW_FAB;
  return f;
}

function field(o) {
  const label = o.label;
  const box = vstack("Поле · " + label, {
    w: o.w,
    fill: COLORS.surface,
    radius: 4,
    stroke: COLORS.outline,
    padL: 12,
    padR: 12,
    padT: 7,
    padB: 7,
    gap: 2,
  });
  box.appendChild(tx(label, { size: 11, color: COLORS.onSurfaceVariant, ls: 0.4 }));
  const row = hstack("Значение", { w: o.w - 24, gap: 8, cross: "CENTER", main: "SPACE_BETWEEN" });
  if (o.value) {
    row.appendChild(tx(o.value, { size: 16, color: COLORS.onSurface }));
  } else {
    row.appendChild(tx(" ", { size: 16, color: COLORS.onSurface }));
  }
  if (o.suffix) row.appendChild(tx(o.suffix, { size: 13, color: COLORS.onSurfaceVariant }));
  box.appendChild(row);
  return box;
}

function actionRow() {
  return hstack("Действия", { w: CARD_W, padT: 0, padB: 8, padL: 8, padR: 8, gap: 8 });
}

const CUSTOMERS = [
  { name: "Zoi Gothi Hamam", phone: "+7 987 654 32 10", balance: "100 ₽" },
  { name: "Ольга Ковалёва", phone: "+7 912 300 41 07", balance: "0 ₽" },
  { name: "Марина Дой", phone: "+7 903 118 22 55", balance: "500 ₽" },
  { name: "Евгения Ю", phone: "+7 926 771 09 34", balance: "600 ₽" },
  { name: "Кукушкина Варвара", phone: "+7 123 456 78 90", balance: "1 200 ₽" },
];

const MEASUREMENTS = [
  { date: "05.10.2022", type: "поясное изделие", name: "штаны" },
  { date: "06.12.2022", type: "плечевое изделие", name: "куртка" },
];

const METRICS = [
  { label: "Обхват груди · ОГ", value: "92" },
  { label: "Обхват талии · ОТ", value: "74" },
  { label: "Обхват бёдер · ОБ", value: "98" },
  { label: "Высота сидения · Вс", value: "27" },
  { label: "Длина талии по спинке · Дтс", value: "42" },
  { label: "Обхват шеи · Ош", value: "36" },
];

function customerListItem(c) {
  const card = hstack("Клиент · " + c.name, {
    w: CARD_W,
    fill: COLORS.surface,
    radius: 8,
    stroke: COLORS.hairline,
    clip: true,
    shadow: true,
  });
  card.appendChild(photo(112, 104, ICON_PERSON, "Фото · " + c.name));
  const info = vstack("Данные", {
    w: CARD_W - 112,
    padL: 16,
    padR: 16,
    padT: 14,
    padB: 14,
    gap: 4,
    fill: COLORS.surface,
  });
  info.appendChild(tx(c.name, { size: 18, weight: "semibold", width: CARD_W - 144 }));
  info.appendChild(tx(c.phone, { size: 14, color: COLORS.onSurfaceVariant }));
  info.appendChild(tx("Баланс: " + c.balance, { size: 14, color: COLORS.onSurfaceVariant }));
  card.appendChild(info);
  return card;
}

function customerHeroCard(c) {
  const card = vstack("Карточка клиента", {
    w: CARD_W,
    fill: COLORS.surface,
    radius: 8,
    stroke: COLORS.hairline,
    clip: true,
    shadow: true,
  });
  card.appendChild(photo(CARD_W, 194, ICON_PHOTO, "Фото клиента"));
  const info = vstack("Данные", { w: CARD_W, pad: 16, gap: 4, fill: COLORS.surface });
  info.appendChild(tx(c.name, { size: 20, weight: "semibold", width: CARD_W - 32 }));
  info.appendChild(tx(c.phone, { size: 14, color: COLORS.onSurfaceVariant }));
  info.appendChild(tx("Баланс: " + c.balance, { size: 14, color: COLORS.onSurfaceVariant }));
  card.appendChild(info);
  const actions = actionRow();
  actions.appendChild(textButton("СНЯТЬ МЕРКИ", COLORS.primary));
  actions.appendChild(textButton("НОВОЕ ИЗДЕЛИЕ", COLORS.primary));
  card.appendChild(actions);
  return card;
}

function measurementTile(m) {
  const tile = vstack("Замер · " + m.name, {
    w: 150,
    h: 150,
    fill: COLORS.surface,
    radius: 8,
    stroke: COLORS.hairline,
    pad: 16,
    main: "SPACE_BETWEEN",
    shadow: true,
  });
  tile.appendChild(tx(m.date, { size: 15, weight: "semibold", width: 118 }));
  tile.appendChild(tx(m.type, { size: 13, color: COLORS.onSurfaceVariant, width: 118 }));
  tile.appendChild(tx(m.name, { size: 14, color: COLORS.onSurfaceVariant, width: 118 }));
  return tile;
}

function formCard(o) {
  const card = vstack(o.title, {
    w: CARD_W,
    fill: COLORS.surface,
    radius: 8,
    stroke: COLORS.hairline,
    clip: true,
    shadow: true,
  });
  card.appendChild(photo(CARD_W, 194, ICON_PHOTO, "Фото клиента"));
  const inner = vstack("Форма", { w: CARD_W, pad: 16, gap: 12, fill: COLORS.surface });
  for (const f of o.fields) inner.appendChild(field({ label: f.label, value: f.value, w: CARD_W - 32 }));
  card.appendChild(inner);
  const actions = actionRow();
  actions.appendChild(textButton("СОХРАНИТЬ", COLORS.primary));
  actions.appendChild(textButton("ОТМЕНИТЬ", COLORS.primary));
  if (o.deletable) actions.appendChild(textButton("УДАЛИТЬ", COLORS.error));
  card.appendChild(actions);
  return card;
}

function screenCustomers() {
  const s = screen("01 · Список клиентов");
  s.appendChild(appBar("Клиенты"));
  const b = body();
  for (const c of CUSTOMERS) b.appendChild(customerListItem(c));
  s.appendChild(b);
  const f = fab(ICON_ADD, "FAB · новый клиент");
  f.x = W - 16 - 56;
  f.y = H - 16 - 56;
  s.appendChild(f);
  return s;
}

function screenCustomerCard() {
  const s = screen("02 · Карточка клиента");
  s.appendChild(appBar("Клиент", true));
  const b = body();
  b.appendChild(customerHeroCard(CUSTOMERS[0]));
  const tiles = hstack("Замеры", { w: CARD_W, gap: 8, clip: true, fill: COLORS.bg });
  for (const m of MEASUREMENTS) tiles.appendChild(measurementTile(m));
  b.appendChild(tiles);
  s.appendChild(b);
  const f = fab(ICON_EDIT, "FAB · редактировать");
  f.x = W - 16 - 56;
  f.y = H - 16 - 56;
  s.appendChild(f);
  return s;
}

function screenAddCustomer() {
  const s = screen("03 · Новый клиент");
  s.appendChild(appBar("Новый клиент", true));
  const b = body();
  b.appendChild(formCard({
    title: "Новый клиент",
    fields: [
      { label: "Имя клиента" },
      { label: "Телефон" },
      { label: "Баланс" },
    ],
  }));
  s.appendChild(b);
  return s;
}

function screenEditCustomer() {
  const s = screen("04 · Редактирование клиента");
  s.appendChild(appBar("Редактирование", true));
  const b = body();
  b.appendChild(formCard({
    title: "Редактирование клиента",
    deletable: true,
    fields: [
      { label: "Имя клиента", value: CUSTOMERS[0].name },
      { label: "Телефон", value: CUSTOMERS[0].phone },
      { label: "Баланс", value: CUSTOMERS[0].balance },
    ],
  }));
  s.appendChild(b);
  return s;
}

function screenAddMeasurement() {
  const s = screen("05 · Новый замер");
  s.appendChild(appBar("Новый замер", true));
  const b = body();
  const card = vstack("Форма замера", {
    w: CARD_W,
    fill: COLORS.surface,
    radius: 8,
    stroke: COLORS.hairline,
    clip: true,
    shadow: true,
  });
  const inner = vstack("Поля", { w: CARD_W, pad: 16, gap: 12, fill: COLORS.surface });
  inner.appendChild(field({ label: "Название замера", value: "штаны", w: CARD_W - 32 }));
  inner.appendChild(field({ label: "Тип изделия", value: "поясное изделие", w: CARD_W - 32 }));
  card.appendChild(inner);
  const actions = actionRow();
  actions.appendChild(textButton("СОХРАНИТЬ", COLORS.primary));
  actions.appendChild(textButton("ОТМЕНИТЬ", COLORS.primary));
  card.appendChild(actions);
  b.appendChild(card);
  for (const m of METRICS) b.appendChild(field({ label: m.label, w: CARD_W, suffix: "см" }));
  s.appendChild(b);
  return s;
}

function screenMeasurementCard() {
  const s = screen("06 · Карточка замера");
  s.appendChild(appBar("Мерки", true));
  const b = body();
  const card = vstack("Заголовок замера", {
    w: CARD_W,
    fill: COLORS.surface,
    radius: 8,
    stroke: COLORS.hairline,
    pad: 16,
    gap: 2,
    shadow: true,
  });
  card.appendChild(tx("05.10.2022", { size: 15, color: COLORS.onSurfaceVariant }));
  card.appendChild(tx("штаны", { size: 26, weight: "semibold", width: CARD_W - 32 }));
  card.appendChild(tx("поясное изделие", { size: 14, color: COLORS.onSurfaceVariant }));
  b.appendChild(card);
  for (const m of METRICS) {
    b.appendChild(field({ label: m.label, value: m.value, w: CARD_W, suffix: "см" }));
  }
  s.appendChild(b);
  const f = fab(ICON_EDIT, "FAB · редактировать замер");
  f.x = W - 16 - 56;
  f.y = H - 16 - 56;
  s.appendChild(f);
  return s;
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

  const builders = [
    ["01 · Клиенты", screenCustomers],
    ["02 · Карточка клиента", screenCustomerCard],
    ["03 · Новый клиент", screenAddCustomer],
    ["04 · Редактирование клиента", screenEditCustomer],
    ["05 · Новый замер", screenAddMeasurement],
    ["06 · Карточка замера", screenMeasurementCard],
  ];

  const frames = builders.map(([label, build], i) => {
    const x = i * (W + GAP);
    const frame = build();
    frame.x = x;
    frame.y = 0;
    page.appendChild(frame);
    const caption = tx(label, { size: 14, weight: "medium", color: COLORS.onSurfaceVariant, ls: 0.4 });
    caption.name = PREFIX + " · подпись";
    caption.x = x;
    caption.y = -34;
    page.appendChild(caption);
    return frame;
  });

  const title = tx("Seamstress — экраны", { size: 44, weight: "bold" });
  title.name = PREFIX + " · заголовок";
  title.x = 0;
  title.y = -150;
  page.appendChild(title);

  const subtitle = tx("Material 2 · 360×800 · фиолетовый акцент #6200EE", {
    size: 16,
    color: COLORS.onSurfaceVariant,
  });
  subtitle.name = PREFIX + " · подзаголовок";
  subtitle.x = 0;
  subtitle.y = -96;
  page.appendChild(subtitle);

  figma.viewport.scrollAndZoomIntoView(frames);
  figma.notify("Готово: 6 экранов");
  figma.closePlugin();
}

main().catch((e) => {
  figma.notify("Ошибка плагина: " + e.message, { error: true, timeout: 10000 });
  figma.closePlugin();
});
