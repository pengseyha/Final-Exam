// Live ID-card preview: posts the form (without saving) to /profiles/preview and
// injects the returned card fragment. The selected photo is rendered client-side
// via FileReader so users see it before saving.
(function () {
    const form = document.getElementById('profileForm');
    const preview = document.getElementById('preview');
    const photoInput = document.getElementById('photoInput');
    if (!form || !preview) return;

    let timer = null;

    async function render() {
        const data = new FormData(form);
        data.delete('photo'); // file is handled separately, client-side
        try {
            const res = await fetch('/profiles/preview', { method: 'POST', body: data });
            if (!res.ok) return;
            preview.innerHTML = await res.text();
            applyLocalPhoto();
        } catch (e) {
            // network hiccup — keep the previous preview
        }
    }

    function applyLocalPhoto() {
        if (!photoInput || !photoInput.files || !photoInput.files[0]) return;
        const reader = new FileReader();
        reader.onload = function (e) {
            let img = preview.querySelector('.photo');
            const placeholder = preview.querySelector('.photo.placeholder');
            if (placeholder) {
                const real = document.createElement('img');
                real.className = 'photo';
                placeholder.replaceWith(real);
                img = real;
            }
            if (img) img.src = e.target.result;
        };
        reader.readAsDataURL(photoInput.files[0]);
    }

    function schedule() {
        clearTimeout(timer);
        timer = setTimeout(render, 300);
    }

    form.addEventListener('input', schedule);
    form.addEventListener('change', schedule);
    render();
})();
