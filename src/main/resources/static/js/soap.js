document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('q');
    const rows = document.getElementById('rows');
    const canvas = document.getElementById('chart');

    if (!form || !rows || !canvas) return;

    // Dátumok alapértékre állítása, elmúlt 30 nap
    const fromInput = form.querySelector('input[name="from"]');
    const toInput = form.querySelector('input[name="to"]');
    const to = new Date();
    const from = new Date();
    from.setDate(to.getDate() - 30);
    const fmt = d => d.toISOString().slice(0, 10);
    if (fromInput && toInput) {
        toInput.value = fmt(to);
        fromInput.value = fmt(from);
    }

    let chart;

    form.addEventListener('submit', async function (e) {
        e.preventDefault();

        const qs = new URLSearchParams(new FormData(form));
        try {
            const res = await fetch('/soap/data?' + qs.toString(), {
                headers: { 'Accept': 'application/json' }
            });
            if (!res.ok) throw new Error('HTTP ' + res.status);
            const data = await res.json();
            if (!Array.isArray(data) || data.length === 0) {
                rows.innerHTML = "";
                if (chart) chart.destroy();
                alert("Nincs adat a megadott tartományra.");
                return;
            }

            // Táblázat
            rows.innerHTML = data
                .map(d => `<tr><td>${d.date}</td><td>${d.value}</td></tr>`)
                .join('');

            // Grafikon
            const labels = data.map(d => d.date);
            const values = data.map(d => d.value);
            if (chart) chart.destroy();
            chart = new Chart(canvas.getContext('2d'), {
                type: 'line',
                data: { labels, datasets: [{ label: qs.get('currency'), data: values }] },
                options: { responsive: true }
            });
        } catch (err) {
            alert('Hiba történt a lekérésnél. Nézd meg a konzolt.');
            console.error(err);
        }
    });
});
