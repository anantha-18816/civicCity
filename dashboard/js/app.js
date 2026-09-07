/* CivicAI Admin Dashboard */
(function () {
  "use strict";

  var API_BASE = localStorage.getItem("civicai.api") || "http://localhost:8080/api";

  var COLORS = {
    bg: { top: "#0b1220", bottom: "#111c2e", card: "#16233a", border: "#24344f" },
    accent: "#22d3ee",
    accent2: "#a78bfa",
    muted: "#8ca3c3",
    green: "#34d399",
    red: "#f87171",
    status: {
      SUBMITTED: "#fbbf24",
      AI_ANALYZED: "#60a5fa",
      ASSIGNED: "#a78bfa",
      IN_PROGRESS: "#fb923c",
      RESOLVED: "#34d399",
      REJECTED: "#f87171",
      VERIFIED: "#2dd4bf",
    },
    priority: { 1: "#f87171", 2: "#fb923c", 3: "#fbbf24", 4: "#64748b" },
    issue: {
      POTHOLE: "#fb923c",
      GARBAGE: "#34d399",
      STREETLIGHT: "#fbbf24",
      WATER_LOGGING: "#60a5fa",
      ROAD_DAMAGE: "#f87171",
      OTHER: "#a78bfa",
    },
  };

  var state = {
    token: localStorage.getItem("civicai.token") || null,
    who: null,
    map: null,
    heat: null,
    currentView: "overview",
  };

  /* ---------------- helpers ---------------- */

  function $(sel) { return document.querySelector(sel); }

  function esc(s) {
    return String(s == null ? "" : s).replace(/[&<>"']/g, function (c) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c];
    });
  }

  function toast(msg, kind) {
    var t = $("#toast");
    t.textContent = msg;
    t.className = "toast " + (kind || "");
    clearTimeout(t._h);
    t._h = setTimeout(function () { t.className = "toast hidden"; }, 3200);
  }

  /* ---------------- officer actions ---------------- */

  function openModal(body) {
    $("#modal-body").innerHTML = body;
    $("#modal").classList.remove("hidden");
  }
  function closeModal() { $("#modal").classList.add("hidden"); }
  $("#modal-close").addEventListener("click", closeModal);
  $("#modal").addEventListener("click", function (e) { if (e.target === $("#modal")) closeModal(); });

  var VALID_STATUS = ["SUBMITTED", "AI_ANALYZED", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "REJECTED"];

  function officerAllowed() {
    return state.who && (state.who.role === "OFFICER" || state.who.role === "ADMIN");
  }

  function complaintDetail(id) {
    var dept = state.depts || [];
    Promise.all([
      api("/complaints/" + id),
      api("/complaints/" + id + "/resolution").then(function (r) { return r; }).catch(function () { return null; }),
    ]).then(function (d) {
      var c = d[0], res = d[1];
      var dname = dept.find(function (x) { return x.id === c.departmentId; });
      var canAssign = officerAllowed() && (!c.departmentId || c.status === "SUBMITTED");
      var canStatus = officerAllowed() && c.status !== "RESOLVED" && c.status !== "REJECTED";

      openModal(
        '<h3 style="margin-bottom:4px">#' + c.id + " · " + esc(c.title) + "</h3>" +
        '<span class="chip" style="color:' + statusColor(c.status) + ";background:" + statusColor(c.status) + "1f;border-color:" + statusColor(c.status) + "55\">" + esc(c.status).replace(/_/g, " ") + "</span>" +
        '<div class="muted small" style="margin-top:6px">' + esc(c.description || "") + "</div>" +
        '<div style="margin-top:14px">' +
        '<div class="row"><span class="muted">Issue type</span><b>' + esc(c.issueType) + "</b></div>" +
        '<div class="row"><span class="muted">Priority</span><b>P' + (c.priority == null ? "—" : c.priority) + "</b></div>" +
        '<div class="row"><span class="muted">Department</span><b>' + (dname ? dname.name + " (" + dname.code + ")" : (c.departmentId || "Unassigned")) + "</b></div>" +
        '<div class="row"><span class="muted">Submitted</span><b>' + esc(fmtDate(c.createdAt)) + "</b></div>" +
        (c.duplicateOfId ? '<div class="row"><span class="muted">Duplicate of</span><b>#' + c.duplicateOfId + (c.duplicateScore != null ? " (" + Math.round(c.duplicateScore * 100) + "%)" : "") + "</b></div>" : "") +
        '<div class="row"><span class="muted">Coordinates</span><b>' + c.latitude.toFixed(5) + ", " + c.longitude.toFixed(5) + "</b></div>" +
        "</div>" +
        (res ? ("<div class='row' style='margin-top:12px'><span class='muted'>Resolution</span><b>" + (res.verificationStatus || "submitted") + "</b></div>") : "") +

        (canStatus ? '<div class="modal-field"><label>Update status</label><div class="modal-actions">' +
          VALID_STATUS.filter(function (s) { return s !== c.status; }).map(function (s) {
            return '<button class="btn-secondary" onclick="window.__civicAI.setStatus(' + c.id + ",''" + s + "''" + ')">' + s.replace(/_/g, " ") + "</button>";
          }).join("") +
          "</div></div>" : "") +

        (canAssign ? '<div class="modal-field"><label>Assign to department</label>' +
          '<select id="assign-select">' + dept.map(function (x) { return "<option value='" + x.id + "'>" + esc(x.name) + "</option>"; }).join("") + "</select>" +
          '<button class="btn-secondary" style="margin-top:10px" onclick="window.__civicAI.assign(' + c.id + ')">Assign</button></div>' : "") +

        (officerAllowed() && c.status === "ASSIGNED" ? '<div class="modal-field"><label>Submit resolved (marks IN_PROGRESS &gt; RESOLVED)</label>' +
          '<input id="res-note" placeholder="Resolution note / outcome"><button class="btn-secondary" style="margin-top:10px" onclick="window.__civicAI.submitResolution(' + c.id + ')">Mark resolved</button></div>' : "")
      );
    }).catch(function (e) { toast("Load failed: " + e.message, "error"); });
  }

  function setStatus(id, status) {
    api("/complaints/" + id + "/status", { method: "PATCH", body: JSON.stringify({ status: status }) })
      .then(function () { closeModal(); toast("Status → " + status.replace(/_/g, " "), "ok"); goto(state.currentView); })
      .catch(function (e) { toast("Update failed: " + e.message, "error"); });
  }

  function assign(id) {
    var sel = $("#assign-select").value;
    api("/complaints/" + id + "/assign", { method: "POST", body: JSON.stringify({ departmentId: Number(sel) }) })
      .then(function () { closeModal(); toast("Assigned", "ok"); goto(state.currentView); })
      .catch(function (e) { toast("Assign failed: " + e.message, "error"); });
  }

  function submitResolution(id) {
    var note = ($("#res-note") || {}).value || "";
    api("/complaints/" + id + "/resolution", { method: "POST", body: JSON.stringify({ notes: note }) })
      .then(function () { closeModal(); toast("Marked resolved", "ok"); goto(state.currentView); })
      .catch(function (e) { toast("Failed: " + e.message, "error"); });
  }

  window.__civicAI = {
    setStatus: setStatus,
    assign: assign,
    submitResolution: submitResolution,
    open: complaintDetail,
  };

  function statusColor(s) { return COLORS.status[s] || COLORS.muted; }
  function prioColor(p) { return COLORS.priority[p] || COLORS.muted; }
  function fmt(num) { return num == null ? "—" : Number(num).toLocaleString(); }
  function fmtDate(iso) {
    if (!iso) return "—";
    var d = new Date(iso);
    return d.toLocaleDateString() + " " + d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  }

  function api(path, opts) {
    opts = opts || {};
    var h = new Headers(opts.headers || {});
    h.set("Content-Type", "application/json");
    if (state.token) h.set("Authorization", "Bearer " + state.token);
    return fetch(API_BASE + path, { method: opts.method || "GET", headers: h, body: opts.body }).then(function (r) {
      if (r.status === 401) { logout(); throw new Error("Session expired"); }
      if (!r.ok) return r.text().then(function (t) { throw new Error(t || ("HTTP " + r.status)); });
      return r.status === 204 ? null : r.json();
    });
  }

  function logout() {
    state.token = null;
    state.who = null;
    localStorage.removeItem("civicai.token");
    showLogin();
  }

  /* ---------------- auth ---------------- */

  function showLogin() { $("#view-app").classList.add("hidden"); $("#view-login").classList.remove("hidden"); }

  function showApp() { $("#view-login").classList.add("hidden"); $("#view-app").classList.remove("hidden"); }

  function doLogin(email, password) {
    var btn = $("#login-btn");
    btn.disabled = true;
    fetch(API_BASE + "/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: email, password: password }),
    })
      .then(function (r) { return r.json().then(function (j) { return { ok: r.ok, j: j }; }); })
      .then(function (res) {
        if (!res.ok || !res.j.token) throw new Error(res.j.message || "Login failed");
        state.token = res.j.token;
        state.who = res.j;
        localStorage.setItem("civicai.token", state.token);
        if (res.j.role !== "OFFICER" && res.j.role !== "ADMIN") throw new Error("This console requires an OFFICER account");
        showApp();
        initNav();
        goto("overview");
        toast("Welcome, " + res.j.name, "ok");
        $("#whoami").textContent = res.j.email + " · " + res.j.role;
      })
      .catch(function (e) {
        var el = $("#login-error");
        el.textContent = e.message;
        el.classList.remove("hidden");
      })
      .finally(function () { btn.disabled = false; });
  }

  $("#login-form").addEventListener("submit", function (e) {
    e.preventDefault();
    doLogin($("#login-email").value.trim(), $("#login-password").value);
  });

  $("#logout").addEventListener("click", logout);

  /* ---------------- navigation ---------------- */

  function initNav() {
    var items = document.querySelectorAll(".nav-item");
    items.forEach(function (b) {
      b.addEventListener("click", function () { goto(b.dataset.view); });
    });
  }

  function goto(view) {
    state.currentView = view;
    document.querySelectorAll(".nav-item").forEach(function (b) {
      b.classList.toggle("active", b.dataset.view === view);
    });
    document.querySelectorAll(".view").forEach(function (s) {
      s.classList.add("hidden");
      if (s.id === "view-" + view) s.classList.remove("hidden");
    });
    if (view === "overview") renderOverview();
    if (view === "map") renderMap();
    if (view === "heatmap") renderHeatmap();
    if (view === "priority") renderPriority();
    if (view === "departments") renderDepartments();
    if (view === "complaints") renderComplaints();
  }

  function card(label, value, color, icon) {
    return '<div class="stat-card"><div class="dot" style="background:' + color + "22;color:" + color + '">' + icon + "</div>" +
      '<div class="value">' + value + '</div><div class="label">' + label + "</div></div>";
  }

  function hbar(label, value, max, color) {
    var pct = max > 0 ? Math.round((value / max) * 100) : 0;
    return '<div class="h-bar"><span class="label">' + esc(label) + "</span>" +
      '<div class="track"><div class="fill" style="width:' + pct + "%;background:" + color + '"></div></div>' +
      '<span class="num">' + fmt(value) + "</span></div>";
  }

  /* ---------------- overview ---------------- */

  function renderOverview() {
    var el = $("#view-overview");
    el.innerHTML = '<div class="card title-card"><h2>Overview</h2><p class="muted small">Live civic operations snapshot.</p></div>';

    Promise.all([api("/complaints"), api("/dashboard/resolution-stats"), api("/dashboard/ai-insights")])
      .then(function (d) {
        var complaints = d[0], res = d[1], ai = d[2];
        var open = complaints.filter(function (c) { return c.status !== "RESOLVED" && c.status !== "REJECTED"; }).length;
        var sum = ai.avgConfidence || 0;

        el.insertAdjacentHTML("beforeend",
          '<div class="stat-grid">' +
          card("Total complaints", fmt(complaints.length), COLORS.accent, "◉") +
          card("Open", fmt(open), COLORS.amber, "◉") +
          card("Resolved", fmt(res.totalResolved), COLORS.green, "◉") +
          card("Verified", fmt(res.verified), COLORS.blue, "◉") +
          card("Rejected", fmt(res.rejected), COLORS.red, "◉") +
          card("Pending review", fmt(res.pendingVerification), COLORS.accent2, "◉") +
          "</div>" +
          "<div class='stat-grid'>" +
          card("Avg resolution", fmt(res.avgResolutionHours) + "h", COLORS.green, "◉") +
          card("AI analyses", fmt(ai.totalAnalyses), COLORS.accent, "◉") +
          card("Avg confidence", Math.round((sum || 0) * 100) + "%", COLORS.blue, "◉") +
          card("Duplicates linked", fmt(ai.duplicatesLinked), COLORS.accent2, "◉") +
          "</div>" +
          '<div class="card" style="grid-column:1/-1"><h3>Complaints by severity</h3><div style="margin-top:14px">' +
          Object.keys(ai.complaintsBySeverity || {}).map(function (k) {
            return hbar(k, ai.complaintsBySeverity[k], Math.max.apply(null, Object.values(ai.complaintsBySeverity || [0])), COLORS.accent);
          }).join("") +
          "</div></div>" +
          '<div class="card" style="grid-column:1/-1"><h3>AI detections</h3><div style="margin-top:14px">' +
          Object.keys(ai.detectionsByObject || {}).map(function (k) {
            return hbar(k, ai.detectionsByObject[k], Math.max.apply(null, Object.values(ai.detectionsByObject || [0])), COLORS.accent2);
          }).join("") +
          "</div></div>");
      })
      .catch(function (e) { el.insertAdjacentHTML("beforeend", '<div class="card">Failed to load: ' + esc(e.message) + "</div>"); });
  }

  /* ---------------- map ---------------- */

  function initMap(id) {
    if (state.map) return state.map;
    state.map = L.map(id).setView([17.385, 78.486], 11);
    L.tileLayer("https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png", {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OSM</a> &copy; CARTO',
      subdomains: "abcd",
      maxZoom: 19,
    }).addTo(state.map);
    setTimeout(function () { state.map.invalidateSize(); }, 60);
    return state.map;
  }

  function renderMap() {
    var map = initMap("map");
    map.eachLayer(function (ly) { if (ly instanceof L.Marker || ly instanceof L.CircleMarker) map.removeLayer(ly); });
    api("/dashboard/map").then(function (pts) {
      var icons = {};
      pts.forEach(function (p) {
        var c = statusColor(p.status);
        if (!icons[c]) {
          icons[c] = L.divIcon({
            className: "",
            html: '<div style="width:16px;height:16px;border-radius:50%;background:' + c + ";border:2px solid #fff;box-shadow:0 0 10px " + c + '"></div>',
            iconSize: [16, 16],
          });
        }
        L.marker([p.latitude, p.longitude], { icon: icons[c] })
          .addTo(map)
          .on("click", function () { complaintDetail(p.id); })
          .bindPopup("<b>" + esc(p.issueType) + "</b><br>" + esc(p.status) +
            "<br>P" + (p.priority == null ? "—" : p.priority) +
            "<br>#<b>" + p.id + "</b> " + esc(p.severity || ""));
      });
    }).catch(function (e) { toast("Map load failed: " + e.message, "error"); });

    api("/clusters").then(function (cl) {
      cl.forEach(function (c) {
        L.circle([c.latitude, c.longitude], {
          radius: 60, color: COLORS.accent, weight: 1,
          fillColor: COLORS.accent, fillOpacity: 0.12,
        }).addTo(map).bindPopup("<b>Cluster #" + c.id + "</b><br>" + esc(c.issueType) +
          "<br>Reports: " + c.reportCount + "<br>Severity: " + esc(c.severity || "—") + "<br>Priority: P" + (c.priority == null ? "—" : c.priority));
      });
    }).catch(function () {});
  }

  /* ---------------- heatmap ---------------- */

  var SEV_W = { SEVERE: 1.2, HIGH: 1.1, MEDIUM: 0.8, LOW: 0.5 };

  function renderHeatmap() {
    var map = initMap("heatmap");
    map.eachLayer(function (ly) { if (ly instanceof L.HeatLayer) map.removeLayer(ly); });
    api("/dashboard/heatmap").then(function (pts) {
      var data = pts
        .filter(function (p) { return p.latitude != null && p.longitude != null; })
        .map(function (p) { return [p.latitude, p.longitude, (p.weight || 1) * (SEV_W[p.severity] || 0.8)]; });
      if (state.heat) map.removeLayer(state.heat);
      if (data.length === 0) { toast("No heatmap data yet", ""); return; }
      state.heat = L.heatLayer(data, { radius: 30, blur: 22, maxZoom: 15, gradient: { 0.2: COLORS.accent, 0.5: COLORS.amber, 0.8: COLORS.red } }).addTo(map);
    }).catch(function (e) { toast("Heatmap load failed: " + e.message, "error"); });
  }

  /* ---------------- priority ---------------- */

  function renderPriority() {
    var el = $("#view-priority");
    el.innerHTML = '<div class="card title-card"><h2>Priority queue</h2><p class="muted small">Ranked by the priority engine — biggest impact first.</p></div>';
    api("/complaints/priority/queue").then(function (q) {
      el.insertAdjacentHTML("beforeend", '<div class="card">' + q.map(function (row) {
        var col = prioColor(row.priority);
        var bar = Math.min(100, Math.round(row.score * 100));
        return '<div class="queue-row">' +
          '<span class="badge-p" style="background:' + col + "22;color:" + col + '">P' + (row.priority == null ? "—" : row.priority) + "</span>" +
          '<div style="flex:1">' +
          '<div style="font-weight:700">#' + row.complaintId + " · " + esc(row.title) + "</div>" +
          '<div class="kv"><span>' + esc(row.issueType) + "</span>" +
          "<span>· " + esc(row.status) + "</span>" +
          "<span>· sev " + esc(row.severity || "—") + "</span>" +
          "<span>· reports " + fmt(row.reportCount) + "</span>" +
          "<span>· " + fmt(row.ageDays) + "d old</span>" +
          (row.roadRisk ? "<span>· " + esc(row.roadRisk) + "</span>" : "") +
          "</div></div>" +
          '<div class="kv" style="flex-direction:column;text-align:right"><b>' + row.score.toFixed(2) + "</b><span>score</span></div>" +
          '<div class="score-bar"><i style="width:' + bar + "%\"></i></div>" +
          "</div>";
      }).join("") || '<p class="muted">Queue is empty.</p>' + "</div>");
    }).catch(function (e) { el.insertAdjacentHTML("beforeend", '<div class="card">Failed: ' + esc(e.message) + "</div>"); });
  }

  /* ---------------- departments ---------------- */

  function renderDepartments() {
    var el = $("#view-departments");
    el.innerHTML = '<div class="card title-card"><h2>Departments</h2><p class="muted small">Open workload by priority.</p></div>';
    Promise.all([api("/departments/workload"), api("/departments")])
      .then(function (d) {
        var workload = d[0], depts = d[1];
        var max = Math.max.apply(null, workload.map(function (w) { return w.totalOpen; }).concat([0]));
        el.insertAdjacentHTML("beforeend", '<div class="dept-grid">' + workload.map(function (w) {
          var dept = depts.find(function (x) { return x.id === w.departmentId; });
          var bars = [1, 2, 3, 4].map(function (p) {
            var v = (w.byPriority && w.byPriority[p]) || 0;
            var pct = max > 0 ? Math.round((v / max) * 100) : 0;
            return '<div><div class="kv"><span>Priority P' + p + '</span><b>' + v + "</b></div>" +
              '<div class="prio-bar"><i style="width:' + pct + "%;background:" + prioColor(p) + '"></i></div></div>';
          }).join("");
          return '<div class="card dept-card"><div class="head"><div><h3>' + esc(w.name) + "</h3>" +
            '<div class="kv"><span>' + (dept ? esc(dept.code) : "") + "</span></div></div>" +
            '<div class="open">' + w.totalOpen + '<span class="muted small"> open</span></div></div>' +
            '<div class="prio-bars">' + bars + "</div></div>";
        }).join("") + "</div>");
      })
      .catch(function (e) { el.insertAdjacentHTML("beforeend", '<div class="card">Failed: ' + esc(e.message) + "</div>"); });
  }

  /* ---------------- complaints ---------------- */

  function renderMyComplaints() {
    var el = $("#view-complaints");
    el.innerHTML = '<div class="card title-card"><h2>Complaints</h2><p class="muted small">Full register, newest first. Click a row for officer actions.</p></div>';
    api("/complaints").then(function (list) {
      list.sort(function (a, b) { return new Date(b.createdAt) - new Date(a.createdAt); });
      el.insertAdjacentHTML("beforeend", '<div class="card"><div class="table-wrap"><table><thead><tr>' +
        "<th>ID</th><th>Title</th><th>Issue</th><th>Status</th><th>Priority</th><th>Department</th><th>Submitted</th></tr></thead><tbody>" +
        list.map(function (c) {
          return "<tr style='cursor:pointer' onclick='window.__civicAI.open(" + c.id + ")'>" +
            "<td>#" + c.id + "</td>" +
            "<td><b>" + esc(c.title) + "</b>" + (c.duplicateOfId ? '<div class="muted small">dup of #' + c.duplicateOfId + "</div>" : "") + "</td>" +
            "<td>" + esc(c.issueType) + "</td>" +
            '<td><span class="chip" style="color:' + statusColor(c.status) + ";background:" + statusColor(c.status) + "1f;border-color:" + statusColor(c.status) + "55\">" + esc(c.status).replace(/_/g, " ") + "</span></td>" +
            '<td><span class="badge-p" style="background:' + prioColor(c.priority) + "22;color:" + prioColor(c.priority) + '">P' + (c.priority == null ? "—" : c.priority) + "</span></td>" +
            "<td>" + (c.departmentId || "—") + "</td>" +
            "<td class='muted small'>" + fmtDate(c.createdAt) + "</td>" +
            "</tr>";
        }).join("") + "</tbody></table></div></div>");
    }).catch(function (e) { el.insertAdjacentHTML("beforeend", '<div class="card">Failed: ' + esc(e.message) + "</div>"); });
  }

  function renderComplaints() {
    ensureDepts().then(renderMyComplaints);
  }

  function ensureDepts() {
    if (state.depts) return Promise.resolve();
    return api("/departments").then(function (d) { state.depts = d; }).catch(function () { state.depts = []; });
  }

  /* ---------------- boot ---------------- */

  if (state.token) {
    api("/complaints")
      .then(function () { showApp(); initNav(); goto("overview"); })
      .catch(function () { showLogin(); });
  } else {
    showLogin();
  }
})();