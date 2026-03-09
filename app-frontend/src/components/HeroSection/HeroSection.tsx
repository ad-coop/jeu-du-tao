import { Link } from "react-router";
import { useTranslation } from "../../i18n";
import logoUrl from "../../assets/images/logo_tao.jpg";
import styles from "./HeroSection.module.css";

export function HeroSection() {
  const { t } = useTranslation();

  return (
    <header className={styles.hero}>
      <img src={logoUrl} alt={t("landing.logo.alt")} className={styles.logo} />
      <h1 className={styles.title}>{t("landing.title")}</h1>
      <p className={styles.subtitle}>{t("landing.subtitle")}</p>
      <p className={styles.intro}>{t("landing.intro")}</p>
      <div className={styles.cta}>
        <Link to="/game/create" className={styles.button}>
          {t("landing.cta.createGame")}
        </Link>
        <Link to="/game/join" className={styles.button}>
          {t("landing.cta.joinGame")}
        </Link>
      </div>
    </header>
  );
}
