import { render, screen } from "@testing-library/react";
import { LandingFooter } from "./LandingFooter";

vi.mock("../../i18n", () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

describe("LandingFooter", () => {
  it("render_showsBothOrganisationBlocks", () => {
    render(<LandingFooter />);

    const images = screen.getAllByRole("img");
    expect(images).toHaveLength(2);
    expect(screen.getByRole("contentinfo")).toBeInTheDocument();
  });

  it("externalLinks_haveCorrectAttributes", () => {
    render(<LandingFooter />);

    const links = screen.getAllByRole("link");
    links.forEach((link) => {
      expect(link).toHaveAttribute("target", "_blank");
      expect(link).toHaveAttribute("rel", "noopener noreferrer");
    });
  });

  it("socialLinks_haveAccessibleNames", () => {
    render(<LandingFooter />);

    expect(
      screen.getByRole("link", {
        name: "landing.footer.taoWorld.facebook.label",
      }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("link", {
        name: "landing.footer.taoWorld.linkedin.label",
      }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("link", {
        name: "landing.footer.adcoop.facebook.label",
      }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("link", {
        name: "landing.footer.adcoop.linkedin.label",
      }),
    ).toBeInTheDocument();
  });
});
