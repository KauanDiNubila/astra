(function () {
  var t = localStorage.getItem("astra_theme")
  document.documentElement.classList.toggle("dark", t !== "light")
})()
