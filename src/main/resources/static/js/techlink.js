document
  .querySelector(".toggle-password")
  ?.addEventListener("click", function () {
    const field = document.getElementById("password");
    const show = field.type === "password";
    field.type = show ? "text" : "password";
    this.textContent = show ? "Ocultar" : "Mostrar";
    this.setAttribute("aria-pressed", String(show));
  });
document.getElementById("sort")?.addEventListener("change", function () {
  const grid = document.getElementById("product-grid");
  const cards = [...grid.children];
  cards.forEach((card, i) => {
    if (!card.dataset.index) card.dataset.index = String(i);
  });
  cards.sort((a, b) =>
    this.value === "price-asc"
      ? Number(a.dataset.price) - Number(b.dataset.price)
      : this.value === "price-desc"
        ? Number(b.dataset.price) - Number(a.dataset.price)
        : this.value === "name"
          ? a.dataset.name.localeCompare(b.dataset.name, "es")
          : Number(a.dataset.index) - Number(b.dataset.index),
  );
  cards.forEach((card) => grid.append(card));
});
document.getElementById("table-filter")?.addEventListener("input", function () {
  const term = this.value.toLocaleLowerCase("es");
  const rows = [...document.querySelectorAll("#inventory tbody tr")];
  rows.forEach((row) => {
    row.hidden = !row.textContent.toLocaleLowerCase("es").includes(term);
  });
  document.getElementById("filter-empty").hidden = rows.some(
    (row) => !row.hidden,
  );
});
document.querySelectorAll("form[data-confirm]").forEach((form) =>
  form.addEventListener("submit", (event) => {
    if (!window.confirm(form.dataset.confirm)) event.preventDefault();
  }),
);
let previewUrl;
document.getElementById("img")?.addEventListener("change", function () {
  const file = this.files[0];
  this.setCustomValidity("");
  if (!file) return;
  if (file.size > 5 * 1024 * 1024) {
    this.setCustomValidity("La imagen debe pesar como máximo 5 MB.");
    this.reportValidity();
    return;
  }
  if (previewUrl) URL.revokeObjectURL(previewUrl);
  previewUrl = URL.createObjectURL(file);
  document.getElementById("image-preview").src = previewUrl;
});
