/* Presentation only. All writes remain native Thymeleaf POST forms. */
(function () {
  "use strict";
  var toggle = document.querySelector(".menu-toggle");
  var nav = document.getElementById("main-nav");
  if (toggle && nav) {
    toggle.addEventListener("click", function () {
      var open = toggle.getAttribute("aria-expanded") !== "true";
      toggle.setAttribute("aria-expanded", String(open));
      nav.classList.toggle("is-open", open);
    });
    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape" && toggle.getAttribute("aria-expanded") === "true") {
        toggle.setAttribute("aria-expanded", "false");
        nav.classList.remove("is-open");
        toggle.focus();
      }
    });
  }
  document.querySelectorAll("form[data-confirm]").forEach(function (form) {
    form.addEventListener("submit", function (event) {
      if (!window.confirm(form.dataset.confirm)) event.preventDefault();
    });
  });
  document.querySelectorAll("[data-date-form]").forEach(function (form) {
    var date = form.querySelector("[name=classDate]");
    var deadline = form.querySelector("[name=deadline]");
    if (!date || !deadline) return;
    function validateDates() {
      deadline.setCustomValidity(date.value && deadline.value && deadline.value > date.value
        ? "모집 마감 일시는 클래스 진행 일시보다 늦을 수 없습니다." : "");
    }
    date.addEventListener("input", validateDates);
    deadline.addEventListener("input", validateDates);
    validateDates();
  });
  var latest = document.querySelector("[data-latest-url]");
  if (latest) {
    // Reuse the existing, public HTML endpoint; no new API or controller required.
    fetch(latest.dataset.latestUrl, { credentials: "same-origin", headers: { "Accept": "text/html" } })
      .then(function (response) {
        if (!response.ok) throw new Error("Class list unavailable");
        return response.text();
      })
      .then(function (html) {
        var page = new DOMParser().parseFromString(html, "text/html");
        var grid = page.getElementById("class-results");
        if (!grid) throw new Error("Class list missing");
        var cards = grid.querySelectorAll(".class-card");
        latest.replaceChildren();
        if (!cards.length) {
          latest.classList.remove("class-grid");
          latest.innerHTML = '<div class="empty-state"><span class="empty-symbol" aria-hidden="true">✦</span><h3>새로운 클래스를 기다리고 있어요</h3><p>나누고 싶은 취미가 있다면 첫 클래스를 열어보세요.</p></div>';
        } else {
          Array.from(cards).slice(0, 4).forEach(function (card) {
            latest.appendChild(document.importNode(card, true));
          });
        }
      })
      .catch(function () {
        latest.classList.remove("class-grid");
        latest.innerHTML = '<div class="notice">클래스를 불러오지 못했어요. 위의 ‘전체 보기’에서 클래스 목록을 확인해주세요.</div>';
      })
      .finally(function () { latest.setAttribute("aria-busy", "false"); });
  }
})();

