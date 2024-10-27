document.addEventListener("DOMContentLoaded", function () {
    const breachAlerts = document.querySelectorAll('.alert-breach');
    if (breachAlerts.length > 0) {
        alert("There are threshold breaches in the weather data!");
    }
});
