# Legal Pages Setup

These files are ready for publishing with GitHub Pages:
- `docs/privacy.html`
- `docs/terms.html`

## 1) Edit placeholders
Update `your-email@example.com` in both files.

## 2) Publish on GitHub Pages
If this repository is public, you can use Pages directly:
1. GitHub -> Settings -> Pages
2. Source: Deploy from branch
3. Branch: `main`, folder: `/docs`

Result URLs for this repository:
- Privacy: `https://kukliskyrylo.github.io/AIScanSort/privacy.html`
- Terms: `https://kukliskyrylo.github.io/AIScanSort/terms.html`

## 3) Update app URLs in one place
Edit constants in:
- `app/src/main/java/com/smartscan/ai/ui/paywall/LegalLinks.kt`

Configured values:
- `TERMS_URL = https://kukliskyrylo.github.io/AIScanSort/terms.html`
- `PRIVACY_URL = https://kukliskyrylo.github.io/AIScanSort/privacy.html`

## 4) Play Console
Add privacy link in Play Console:
- Policy and programs -> App content -> Privacy policy
