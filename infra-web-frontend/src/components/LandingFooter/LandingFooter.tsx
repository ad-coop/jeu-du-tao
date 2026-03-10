import { useTranslation } from "../../i18n";
import logoUrl from "../../assets/images/logo_tao.jpg";
import adcoopWhiteUrl from "../../assets/images/adcoop-white.png";
import styles from "./LandingFooter.module.css";

function FacebookIcon() {
  return (
    <svg
      aria-hidden="true"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="currentColor"
    >
      <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z" />
    </svg>
  );
}

function LinkedInIcon() {
  return (
    <svg
      aria-hidden="true"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="currentColor"
    >
      <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z" />
    </svg>
  );
}

export function LandingFooter() {
  const { t } = useTranslation();

  return (
    <footer className={styles.footer}>
      <div className={styles.grid}>
        <div className={styles.block}>
          <img
            src={logoUrl}
            alt={t("landing.logo.alt")}
            className={styles.logo}
          />
          <a
            href="https://www.taoworld.fr"
            target="_blank"
            rel="noopener noreferrer"
            className={styles.link}
          >
            {t("landing.footer.taoWorld.websiteLabel")}
          </a>
          <nav aria-label={t("landing.footer.taoWorld.title")} className={styles.socials}>
            <a
              href="https://www.facebook.com/LeJeuDuTao"
              target="_blank"
              rel="noopener noreferrer"
              aria-label={t("landing.footer.taoWorld.facebook.label")}
              className={styles.socialLink}
            >
              <FacebookIcon />
            </a>
            <a
              href="https://www.linkedin.com/company/jeu-du-tao/"
              target="_blank"
              rel="noopener noreferrer"
              aria-label={t("landing.footer.taoWorld.linkedin.label")}
              className={styles.socialLink}
            >
              <LinkedInIcon />
            </a>
          </nav>
        </div>

        <div className={styles.block}>
          <img
            src={adcoopWhiteUrl}
            alt={t("landing.footer.adcoop.logo.alt")}
            className={styles.logo}
          />
          <a
            href="https://www.adcoop.fr"
            target="_blank"
            rel="noopener noreferrer"
            className={styles.link}
          >
            {t("landing.footer.adcoop.websiteLabel")}
          </a>
          <nav aria-label={t("landing.footer.adcoop.title")} className={styles.socials}>
            <a
              href="https://www.facebook.com/adcoop.alban.dericbourg"
              target="_blank"
              rel="noopener noreferrer"
              aria-label={t("landing.footer.adcoop.facebook.label")}
              className={styles.socialLink}
            >
              <FacebookIcon />
            </a>
            <a
              href="https://www.linkedin.com/company/adcoop/"
              target="_blank"
              rel="noopener noreferrer"
              aria-label={t("landing.footer.adcoop.linkedin.label")}
              className={styles.socialLink}
            >
              <LinkedInIcon />
            </a>
          </nav>
        </div>
      </div>
    </footer>
  );
}
