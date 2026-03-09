import { useTranslation } from "../../i18n";
import { useToast } from "../Toast";
import logoUrl from "../../assets/images/logo_tao.jpg";
import styles from "./HeroSection.module.css";

export function HeroSection() {
  const { t } = useTranslation();
  const { showToast } = useToast();

  const handleCta = () => showToast(t("landing.toast.comingSoon"));

  return (
    <header className={styles.hero}>
      <img
        src={logoUrl}
        alt={t("landing.logo.alt")}
        className={styles.logo}
      />
      <h1 className={styles.title}>{t("landing.title")}</h1>
      <p className={styles.subtitle}>{t("landing.subtitle")}</p>
      <p className={styles.intro}>{t("landing.intro")}</p>
      <div className={styles.cta}>
        <button className={styles.button} onClick={handleCta}>
          {t("landing.cta.createGame")}
        </button>
        <button className={styles.button} onClick={handleCta}>
          {t("landing.cta.joinGame")}
        </button>
      </div>
    </header>
  );
}
